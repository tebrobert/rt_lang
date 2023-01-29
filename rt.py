import argparse
import os
from utils.ASSERT import *
from lang.lib_6_run import *

def readFile(filePath):
    with open(filePath) as f:
        return f.read()

class ArgParser:
    def __init__(self):
        self.argParser = argparse.ArgumentParser()
    def add(self, x, **kwargs):
        self.argParser.add_argument(x, **kwargs)
        return self
    def parse(self):
        return self.argParser.parse_args()

def printHeaderedIfDev(dev, header):
    return lambda value: print(f"{header}:\n{value}\n\n" if dev else "", end="")

def unsafeRunCode(code, dev):
    try:
        print(f"1_CODE:\n{code}\n" if dev else "", end="")
        desugared = flattap(lambda: desugar(code), printHeaderedIfDev(dev, "2_DESUGARED"))
        tokens = flattap(lambda: lexx(desugared), printHeaderedIfDev(dev, "3_TOKENS"))
        expr = flattap(lambda: parse(tokens), printHeaderedIfDev(dev, "4_EXPR"))
        typed = flattap(lambda: sem(expr), printHeaderedIfDev(dev, "5_TYPED"))
        shown = flattap(lambda: show(typed), printHeaderedIfDev(dev, "6_SHOWN"))
        built = build(shown)
        print("7_RUNNING:\n" if dev else "", end="")
        unsafeRunBuilt(built)
    except Exception as e:
        print(e)

def main():
    args = (ArgParser()
        .add("code", nargs="?")
        .add("--dev", action="store_true")
        .add("--test", action="store_true")
        .parse()
    )

    if args.test:
        pathTests = 'tests/'
        path1Code      = '1_code.rt.txt'
        path2Desugared = '2_desugared.rt.txt'
        path3Tokens    = '3_tokens.py.txt'
        path4Expr      = '4_expr.py.txt'
        path5Typed     = '5_typed.txt'
        path6Shown     = '6_shown.py.txt'
        for currentElement in sorted(os.listdir(pathTests)):
            pathCurrentTest = f"{pathTests}{currentElement}/"
            print(currentElement, end=": ")
            try:
                code = readFile(f"{pathCurrentTest}/{path1Code}")

                desugared = TRY_ASSERT("desugared", readFile(f"{pathCurrentTest}/{path2Desugared}"), desugar, code)

                tokens = lexx(desugared)
                assert str(tokens) == readFile(f"{pathCurrentTest}/{path3Tokens}"), f"!=tokens, got: {tokens}"

                expr = parse(tokens)
                assert str(expr) == readFile(f"{pathCurrentTest}/{path4Expr}"), f"!=expr, got: {expr}"

                typed = sem(expr)
                assert str(typed) == readFile(f"{pathCurrentTest}/{path5Typed}"), f"!=typed, got: {typed}"

                shown = show(typed)
                assert shown == readFile(f"{pathCurrentTest}/{path6Shown}"), f"!=shown, got: {shown}"

                _ = build(shown)
                print("PASSED")
            except AssertionError as e:
                print("FAILED: "+str(e))
            except Exception as e:
                print("FAILED: "+str(e))
    elif args.code is not None:
        unsafeRunCode(code=readFile(args.code), dev=args.dev)

if __name__ == "__main__":
    main()