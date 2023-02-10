import argparse
import os
import sys
from utils.ASSERT import *
from lang.lib_6_run import *

def readFile(filePath):
    with open(filePath) as f:
        return f.read()

class ArgParser:
    def __init__(self):
        self.argParser = argparse.ArgumentParser()

    def add(self, x, **kwargs):
        newParser = ArgParser()
        newParser.argParser = self.argParser
        newParser.argParser.add_argument(x, **kwargs)
        return newParser

    def parse(self, argLine):
        argv = [] if argLine=="" else argLine.split(" ")
        return self.argParser.parse_args(argv)

def printIf(cond, value):
    if cond:
        print(value)

def printHeaderIf(cond, header):
    printIf(cond, f"{header}:")

def printHeaderedIf(cond):
    return lambda header: lambda value: \
        (printHeaderIf(cond, header), printIf(cond, f"{value}\n"))

def doMbHeadered(action, value, mbPrintHeadered):
    return flattap(lambda: action(value), mbPrintHeadered)

def assertedDesugar(readResult, code):
    return TRY_ASSERT_EQUAL("desugared",
        readResult("2_desugared.rt.txt"), lambda: desugar(code)
    )

def assertedLexx(readResult, desugared):
    return TRY_ASSERT_EQUAL("tokens",
        readResult("3_tokens.py.txt"), lambda: lexx(desugared)
    )

def assertedParse(readResult, tokens):
    return TRY_ASSERT_EQUAL("expr",
        readResult("4_expr.py.txt"), lambda: parse(tokens)
    )

def assertedSem(readResult, expr):
    return TRY_ASSERT_EQUAL("typed",
        readResult("5_typed.txt"), lambda: sem(expr)
    )

def assertedShow(readResult, typed):
    return TRY_ASSERT_EQUAL("shown",
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
        printHeaderedIfDev = printHeaderedIf(dev)
        printHeaderedIfDev("1_CODE")(code)
        desugared = doMbHeadered(desugar, code, printHeaderedIfDev("2_DESUGARED"))
        tokens = doMbHeadered(lexx, desugared, printHeaderedIfDev("3_TOKENS"))
        expr = doMbHeadered(parse, tokens, printHeaderedIfDev("4_EXPR"))
        typed = doMbHeadered(sem, expr, printHeaderedIfDev("5_TYPED"))
        shown = doMbHeadered(show, typed, printHeaderedIfDev("6_SHOWN"))
        built = build(shown)
        printHeaderIf(dev, "7_RUNNING")
        unsafeRunBuilt(built)
    except Exception as e:
        print(e)

argParser = ArgParser() \
    .add("code", nargs="?") \
    .add("--dev", action="store_true") \
    .add("--test", action="store_true") \

def main(argLine):
    args = argParser.parse(argLine)
    if args.test:
        runTests()
    elif args.code is not None:
        unsafeRunCode(code=readFile(args.code), dev=args.dev)

def promtArgs():
    return input("Enter comand line args: ")

if __name__ == "__main__":
    argLine = (promtArgs() if len(sys.argv) == 1 else
        "".join(sys.argv[1:])
    )
    main(argLine)