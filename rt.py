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

args = ArgParser() \
    .add("code", nargs="?") \
    .add("--dev", action="store_true") \
    .add("--test", action="store_true") \
    .parse()

def printHeaderedIf(cond):
    return lambda header: lambda value: \
        print(f"{header}:\n{value}\n\n" if cond else "", end="")

def doMbHeadered(action, value, mbPrintHeadered):
    return flattap(
        lambda: action(value),
        mbPrintHeadered
    )

def assertedDesugar(readResult, code):
    return TRY_ASSERT("desugared",
        readResult("2_desugared.rt.txt"), lambda: desugar(code)
    )

def assertedLexx(readResult, desugared):
    return TRY_ASSERT("tokens",
        readResult("3_tokens.py.txt"), lambda: lexx(desugared)
    )

def assertedParse(readResult, tokens):
    return TRY_ASSERT("expr",
        readResult("4_expr.py.txt"), lambda: parse(tokens)
    )

def assertedSem(readResult, expr):
    return TRY_ASSERT("typed",
        readResult("5_typed.txt"), lambda: sem(expr)
    )

def assertedShow(readResult, typed):
    return TRY_ASSERT("shown",
        readResult("6_shown.py.txt"), lambda: show(typed)
    )

def runTests():
    pathTests = "tests/"
    for currentElement in sorted(os.listdir(pathTests)):
        pathCurrentTest = f"{pathTests}{currentElement}/"
        readCurrentTestFile = lambda fileName: \
            readFile(f"{pathCurrentTest}/{fileName}")
        print(currentElement, end=": ")
        try:
            code = readCurrentTestFile("1_code.rt.txt")
            desugared = assertedDesugar(readCurrentTestFile, code)
            tokens = assertedLexx(readCurrentTestFile, desugared)
            expr = assertedParse(readCurrentTestFile, tokens)
            typed = assertedSem(readCurrentTestFile, expr)
            _shown = assertedShow(readCurrentTestFile, typed)
            print("PASSED")
        except AssertionError as e:
            print(f"FAILED: {e}")
        except Exception as e:
            print(f"FAILED: {e}")

def unsafeRunCode(code, dev):
    try:
        print(f"1_CODE:\n{code}\n" if dev else "", end="")
        printHeaderedIfDev = printHeaderedIf(dev)
        desugared = doMbHeadered(desugar, code, printHeaderedIfDev("2_DESUGARED"))
        tokens = doMbHeadered(lexx, desugared, printHeaderedIfDev("3_TOKENS"))
        expr = doMbHeadered(parse, tokens, printHeaderedIfDev("4_EXPR"))
        typed = doMbHeadered(sem, expr, printHeaderedIfDev("5_TYPED"))
        shown = doMbHeadered(show, typed, printHeaderedIfDev("6_SHOWN"))
        built = build(shown)
        print("7_RUNNING:\n" if dev else "", end="")
        unsafeRunBuilt(built)
    except Exception as e:
        print(e)

def main():
    if args.test:
        runTests()
    elif args.code is not None:
        unsafeRunCode(code=readFile(args.code), dev=args.dev)

if __name__ == "__main__":
    main()