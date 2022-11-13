from run import *
from sys import argv
from os import system

def run_code(code):
    try:
        if dev(): system("cls")
        if dev(): print(f'code:\n{code}\n')
        
        desugared = desugar(code)
        if dev(): print(f'desugared:\n{desugared}\n')
        
        tokens = lexx(desugared)
        if dev(): print(f'tokens:\n[\n',*map(lambda t: f'    {t},\n',tokens),']\n', sep='')
        
        expr = parse(tokens) 
        if dev(): print(f'expr:\n{expr}\n')
        
        typed = sem(expr)
        if dev(): print(f'typed:\n{typed}\n')
        
        shown = show(typed)
        if dev(): print(f'shown:\n{shown}\n')
        
        program = compile(shown)
        
        if dev(): print('Running...')
        unsafe_run(program)

    except Exception as e:
        print(e.msg)

def app():
    if len(argv) == 1:
        test_code = get_test_code()
        code = test_code if test_code != () else input('Code: ')
        if test_code != (): print(f"Code: {code}")
        run_code(code)
    elif len(argv) >= 2:
        code_path = argv[1]
        with open(code_path) as f:
            code = f.read()
            run_code(code)
    elif len(argv) == 3 and argv[1] == "--code":
        code = argv[2]
        run_code(code)
    else:
        rt = "rt"
        print(
f"""
Usage:
    {rt}
    {rt} <code_path>
    {rt} --code <code>
"""
        )

def dev():
    return "--dev" in argv

def get_test_code():
    return (
        #"input"
        #"(s => s)(input)"
        #"(s => s)(s => s)(input)"
        #"(s => s)((s => s)(input))"
        #"flatmap(s => print(s))(input)"
    )

if __name__ == "__main__":
    app()

