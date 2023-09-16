from utils.fail import *
from utils.tailrec import *


class DesugarErr(ValueError):
    def __init__(self, msg):
        self.msg = f"DesugarErr: {msg}"

    def __repr__(self):
        return self.msg

    @staticmethod
    def fail_if(cond, msg):
        if cond:
            fail(DesugarErr(msg))


def arrow_split(line):
    def force_arrow_split(line_with_arrow):
        idx = line_with_arrow.index("<-")
        arg = line_with_arrow[:idx]
        monad = line_with_arrow[idx + 2:]
        DesugarErr.fail_if("<-" in monad,
            "Can't have more than one `<-` in a line."
        )
        return arg, monad

    return (("_", line) if "<-" not in line else
            force_arrow_split(line_with_arrow=line)
            )


@tailrec
def flatmapize(arrow_split_init_lines, flapmapized):
    def force_flatmapize():
        arg, monad = arrow_split_init_lines[-1]
        return rec(arrow_split_init_lines[:-1],
            f"flatmap({arg} => {flapmapized})({monad})"
        )

    return flapmapized if arrow_split_init_lines == [] else force_flatmapize()


@tailrec
def de_eq(lines, de_eq_lines):
    def force_de_eq(line):
        idx = line.index("=")
        left = line[:idx]
        right = line[idx + 1:]
        DesugarErr.fail_if("=" in right,
            "Can't have more than one `=` in a line."
        )
        return f"{left} <- pure({right})"

    return (de_eq_lines if lines == [] else
            rec(lines[1:], de_eq_lines + [
                lines[0] if not has_eq(lines[0]) else force_de_eq(lines[0])
            ]
            )
            )


def has_eq(line):
    return (line.count("=") > line.count("=>")
            + line.count("==")
            + line.count("!=")
            + line.count(">=")
            + line.count("<=")
            )


def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))
    DesugarErr.fail_if(lines == [], "Yet empty file is unsupported")
    DesugarErr.fail_if("<-" in lines[-1], errMsgBadLastLineArrow)
    DesugarErr.fail_if(has_eq(lines[-1]), errMsgBadLastLineEq)
    de_eq_lines = de_eq(lines, [])
    return flatmapize(list(map(arrow_split, de_eq_lines[:-1])), de_eq_lines[-1])


errMsgBadLastLineArrow = \
    "The last line can't contain `<-`, you'd probably like to remove it."
errMsgBadLastLineEq = \
    "The last line can't contain `=`, you'd probably like to remove it."
