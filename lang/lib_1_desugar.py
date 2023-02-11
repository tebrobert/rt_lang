from utils.fail import *
from utils.tailrec import *

class DesugarErr(ValueError):
    def __init__(self, msg):
        self.msg = f"DesugarErr: {msg}"

    def __repr__(self):
        return self.msg

    @staticmethod
    def failIf(cond, msg):
        if cond:
            fail(DesugarErr(msg))

def arrowSplit(line):
    def forceArrowSplit(lineWithArrow):
        idx = lineWithArrow.index("<-")
        arg = lineWithArrow[:idx]
        monad = lineWithArrow[idx+2: ]
        DesugarErr.failIf("<-" in monad,
            "Can't have more than one `<-` in a line."
        )
        return (arg, monad)
    return (("_", line) if "<-" not in line else
        forceArrowSplit(lineWithArrow=line)
    )

@tailrec
def flatmapize(arrowSplitInitLines, flapmapized):
    def forceFlatmapize():
        arg, monad = arrowSplitInitLines[-1]
        return rec(arrowSplitInitLines[:-1],
            f"flatmap({arg} => {flapmapized})({monad})"
        )
    return flapmapized if arrowSplitInitLines == [] else forceFlatmapize()

@tailrec
def deEq(lines, deEqLines):
    def forceDeEq(line):
        idx = line.index("=")
        left = line[:idx]
        right = line[idx+1: ]
        DesugarErr.failIf("=" in right,
            "Can't have more than one `=` in a line."
        )
        return f"{left} <- pure({right})"

    return (deEqLines if lines == [] else
        rec(lines[1:], deEqLines + [
            lines[0] if not hasEq(lines[0]) else forceDeEq(lines[0])
        ])
    )

errMsgBadLastLineArrow = \
    "The last line can't contain `<-`, you'd probably like to remove it."
errMsgBadLastLineEq = \
    "The last line can't contain `=`, you'd probably like to remove it."

def hasEq(line):
    return ( line.count("=") > line.count("=>")
        + line.count("==")
        + line.count("!=")
        + line.count(">=")
        + line.count("<=")
    )

def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))
    DesugarErr.failIf(lines == [], "Yet empty file is unsupported")
    DesugarErr.failIf("<-" in lines[-1], errMsgBadLastLineArrow)
    DesugarErr.failIf(hasEq(lines[-1]), errMsgBadLastLineEq)
    deEqLines = deEq(lines, [])
    return flatmapize(list(map(arrowSplit, deEqLines[:-1])), deEqLines[-1])