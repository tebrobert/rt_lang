from run import *
from sys import argv
from os import system
import argparse

class FunctionalArgParser():
    def __init__(self):
        self.argParser = argparse.ArgumentParser()
    def add(self, x, **kwargs):
        self.argParser.add_argument(x, **kwargs)
        return self
    def parse(self):
        return self.argParser.parse_args()

def run_code(code, dev):
    try:
        if dev: system("cls")
        if dev: print(f'CODE:\n{code}\n')
        
        desugared = desugar(code)
        if dev: print(f'DESUGARED:\n{desugared}\n')
        
        tokens = lexx(desugared)
        if dev: print(f'TOKENS:\n[\n',*map(lambda t: f'    {t},\n',tokens),']\n', sep='')
        
        expr = parse(tokens) 
        if dev: print(f'EXPR:\n{expr}\n')
        
        typed = sem(expr)
        if dev: print(f'TYPED:\n{typed}\n')
        
        shown = show(typed)
        if dev: print(f'SHOWN:\n{shown}\n')
        
        program = compile(shown)
        
        if dev: print('RUNNING:')
        unsafe_run(program)

    except Exception as e:
        print(e.msg)

def main():
    args = (FunctionalArgParser()
        .add("code")
        .add("--dev", action="store_true")
    ).parse()

    with open(args.code) as f:
        code = f.read()
        run_code(code=code, dev=args.dev)

if __name__ == "__main__":
    main()

