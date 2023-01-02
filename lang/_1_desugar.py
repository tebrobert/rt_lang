from utils.fail import *

class DesugarErr(ValueError):
    def __init__(self, msg):
        self.msg = f"DesugarErr: {msg}"

    def __repr__(self):
        return self.msg

def arrowize(line):
    if "<-" in line:
        idx = line.index("<-")
        return (True, line[:idx], line[idx+2:])
    else:
        return (False, "_", line)

#needs to be tailrec
def flatmapize(arrowized_init_lines, last_line):
    if arrowized_init_lines == []:
        return last_line
    _, current_arg, current_monad = arrowized_init_lines[0]
    return f"flatmap({current_arg} => {flatmapize(arrowized_init_lines[1:], last_line)})({current_monad})"

def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))
    return (
        fail(DesugarErr("Yet empty file is unsupported"))
            if lines == [] else
        fail(DesugarErr("The last line can't contain `<-`, you'd probably like to remove it."))
            if arrowize(lines[-1])[0] else
        flatmapize(list(map(arrowize, lines[:-1])), lines[-1])
    )

