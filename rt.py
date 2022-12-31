import argparse
import os
from modules.run import *

def readFile(filePath):
    with open(filePath) as f: return f.read()

class ArgParser():
    def __init__(self): self.argParser = argparse.ArgumentParser()
    def add(self, x, **kwargs): self.argParser.add_argument(x, **kwargs); return self
    def parse(self): return self.argParser.parse_args()

def unsafeRunCode(code, dev):
    if dev: print(f'1_CODE:\n{code}')
    try:
        desugared = desugar(code); print(f"2_DESUGARED:\n{desugared}\n" if dev else "", end="")
        tokens = lexx(desugared); print(f"3_TOKENS:\n{tokens}\n" if dev else "", end="")
        expr = parse(tokens); print(f"4_EXPR:\n{expr}\n" if dev else "", end="")
        typed = sem(expr); print(f"5_TYPED:\n{typed}\n" if dev else "", end="")
        shown = show(typed); print(f"6_SHOWN:\n{shown}\n" if dev else "", end="")
        compiled = compile(shown); print("7_RUNNING:\n" if dev else "", end="")
        unsafeRunCompiled(compiled)
    except Exception as e: print(e.msg)

def main():
    args = ArgParser().add("code", nargs="?").add("--dev", action="store_true").add("--test", action="store_true").parse()

    if args.test:
        pathTests = 'tests/'
        path1Code      = '1_code.rt'
        path2Desugared = '2_desugared.rt'
        path3Tokens    = '3_tokens.py'
        path4Expr      = '4_expr.py'
        path5Typed     = '5_typed.txt'
        path6Shown     = '6_shown.py'
        for currentElement in os.listdir(pathTests):
            pathCurrentTest = f"{pathTests}{currentElement}/"
            print(currentElement, end=": ")
            try:
                code = readFile(f"{pathCurrentTest}/{path1Code}")
                desugared = desugar(code); assert desugared == readFile(f"{pathCurrentTest}/{path2Desugared}"), f"!=desugared, got: {desugared}"
                tokens = lexx(desugared); assert str(tokens) == readFile(f"{pathCurrentTest}/{path3Tokens}"), f"!=tokens, got: {tokens}"
                expr = parse(tokens); assert str(expr) == readFile(f"{pathCurrentTest}/{path4Expr}"), f"!=expr, got: {expr}"
                typed = sem(expr); assert str(typed) == readFile(f"{pathCurrentTest}/{path5Typed}"), f"!=typed, got: {typed}"
                shown = show(typed); assert shown == readFile(f"{pathCurrentTest}/{path6Shown}"), f"!=shown, got: {shown}"
                compiled = compile(shown)
                print("PASSED")
            except AssertionError as e: print("FAILED: "+str(e))
            except Exception as e: print("FAILED: "+str(e))
    elif args.code is not None: unsafeRunCode(code=readFile(args.code), dev=args.dev)

if __name__ == "__main__": main()