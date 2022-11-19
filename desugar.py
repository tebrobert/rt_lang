class DesugarErr(ValueError):
    def __init__(self, msg):
        self.msg = f"DesugarErr: {msg}"

def desugar(code):
    lines = list(filter(lambda line: line != "", code.split("\n")))

    if lines == []:
        raise DesugarErr("Yet empty file is unsupported")
    
    def arrowize(line):
        i = 0
        char = line[0]
        while "a" <= char <= "z" or "A" <= char <= "Z"  or "0" <= char <= "9" or char == "_":
            i += 1
            if i == len(line):
                return (False, "_", line)
            char = line[i]
        while char == " ":
            i += 1
            if i == len(line):
                return (False, "_", line)
            char = line[i]
        if line[i] != "<":
            return (False, "_", line)
        i += 1
        if i == len(line):
            return (False, "_", line)
        if line[i] != "-":
            return (False, "_", line)
        i += 1
        return (True, line[:i-2], line[i:])
    
    last_line = lines[-1]

    if arrowize(last_line)[0]:
        raise DesugarErr("The last line can't contain `<-`, you'd probably like to remove it.")
    
    arrowized_init_lines = list(map(arrowize, lines[:-1]))
    
    def flatmapize(arrowized_init_lines, last_line):
        if arrowized_init_lines == []:
            return last_line
        _, current_arg, current_monad = arrowized_init_lines[0]
        arrowized_rest_lines = arrowized_init_lines[1:]
        return f"flatmap({current_arg} => {flatmapize(arrowized_rest_lines, last_line)})({current_monad})"
    
    return flatmapize(arrowized_init_lines, last_line)
