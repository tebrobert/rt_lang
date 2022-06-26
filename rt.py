from run import *
from sys import argv
from os import system

def run_code(code):
    try:
        tokens = lexx(code)
        expr = parse(tokens) 
        typed = sem(expr)
        shown = show(typed)
        program = compile(shown)
        unsafe_run(program)
    except Exception as e:
        print(e.msg)

def run_code_dev(code):
    try:
        system("cls")
        print(f'code:\n{code}\n')
        
        tokens = lexx(code)
        print(f'tokens:\n[\n',*map(lambda t: f'    {t},\n',tokens),']\n', sep='')
        
        expr = parse(tokens) 
        print(f'expr:\n{expr}\n')
        
        typed = sem(expr)
        print(f'typed:\n{typed}\n')
        
        shown = show(typed)
        print(f'shown:\n{shown}\n')
        
        program = compile(shown)
        
        print('Running...')
        unsafe_run(program)
        print('Program finished.')
    except Exception as e:
        print(e.msg)

def app():
    if len(argv) == 1:
        code = input('Code: ')
        print('Running...')
        run_code(code)
    elif len(argv) == 3 and argv[1] == "--code_path":
        code_path = argv[2]
        with open(code_path) as f:
            code = read(f)
            run_code(code)
    elif len(argv) == 3 and argv[1] == "--code":
        code = argv[2]
        run_code(code)
    elif len(argv) == 3 and argv[1] == "--dev":
        code = argv[2]
        run_code_dev(code)
    else:
        rt = "rt"
        print(
f"""
Usage:
    {rt}
    {rt} --code_path <code_path>
    {rt} --code <code>
    {rt} --dev <code>
"""
        )

if __name__ == "__main__":
    prod = True
    if prod: app()
    else: run_code('flatmap(s => print(s))(input)')
