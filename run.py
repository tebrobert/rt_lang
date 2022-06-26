from compile import *

class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

def unsafe_run(rio):
    type_rio = type(rio)

    if type_rio is Input:
        return input()

    elif type_rio is Print:
        return print(rio.s)

    elif type_rio is Flatmap:
        return unsafe_run(rio.a_fb(unsafe_run(rio.fa)))

    else:
        raise RunErr(f'Unexpected type "{type_rio}" "{rio}".')

if __name__ == '__main__':
    #print('Reading code...')
    code = [
        'input',
        'flatmap(s => print(s))(input)',
        'flatmap(s => flatmap(u => print(s))(print(s)))(input)',
    ][2]
    #print(f'code:\n{code}\n')
    
    #print('Lexemizing...')
    tokens = lexx(code)
    #print(f'tokens:\n[\n',*map(lambda t: f'    {t},\n',tokens),']\n', sep='')
    
    #print('Parsing...')
    expr = parse(tokens) 
    #print(f'expr:\n{expr}\n')
    
    #print('Type checking...')
    typed = sem(expr)
    print(f'typed:\n{typed}\n')
    
    #print('Compiling...')
    shown = show(typed)
    print(f'shown:\n{shown}\n')
    
    print('Evaluating...')
    program = compile(shown)
    
    print('Running...')
    unsafe_run(program)
    print('Program finished.')
