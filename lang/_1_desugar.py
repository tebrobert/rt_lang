from utils.fail import *
from utils.tailrec import *

class DesugarErr(ValueError):
    def __init__(self, msg):
        self.msg = f"DesugarErr: {msg}"

    def __repr__(self):
        return self.msg

def arrow_split(line):
    if "<-" in line:
        idx = line.index("<-")
        return (line[:idx], line[idx+2:])
    return ("_", line)

@tailrec
def flatmapize(arrow_split_init_lines, flapmapized):
    if arrow_split_init_lines == []:
        return flapmapized
    arg, monad = arrow_split_init_lines[-1]
    return rec(arrow_split_init_lines[:-1], f"flatmap({arg} => {flapmapized})({monad})")

def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))
    return (
        fail(DesugarErr("Yet empty file is unsupported"))
            if lines == [] else
        fail(DesugarErr("The last line can't contain `<-`, you'd probably like to remove it."))
            if "<-" in lines[-1] else
        flatmapize(list(map(arrow_split, lines[:-1])), lines[-1])
    )

