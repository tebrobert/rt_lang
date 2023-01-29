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
        monad = lineWithArrow[idx + 2:]
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

errMsgBadLastLineArrow = \
    "The last line can't contain `<-`, you'd probably like to remove it."

def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))
    DesugarErr.failIf(lines == [], "Yet empty file is unsupported")
    DesugarErr.failIf("<-" in lines[-1], errMsgBadLastLineArrow)
    return flatmapize(list(map(arrowSplit, lines[:-1])), lines[-1])