from lang.lib_6_run import *
from tests.test import *
from utils.arg_parser import *
from utils.flattap import *
from utils.print_if import *
from utils.read_file import *

"""
Steps:
...

Shortcuts:
...
"""


def get_runner(dev):
    def run(action, value, header, show_res=True):
        print_if(dev)(header)
        return flattap(lambda: action(value),
            lambda res: print_if(dev and show_res)(res, end="\n\n")
        )

    return run


def unsafe_run_code(code, dev):
    run = get_runner(dev)
    print_if(dev)("1_CODE")
    print_if(dev)(code)
    try:
        desugared = run(desugar, code, "2_DESUGARED")
        tokens = run(tokenize, desugared, "3_TOKENS")
        expr = run(parse, tokens, "4_EXPR")
        typed = run(typify, expr, "5_TYPED")
        shown = run(build, typed, "6_SHOWN")
        run(unsafe_run_built, rt_compile(shown), "7_RUNNING",
            show_res=False
        )
    except Exception as e:
        print(e)


def main():
    args = argParser.parse(get_args())
    if args.test:
        run_tests()
    elif args.code:
        unsafe_run_code(code=read_file(args.code), dev=args.dev)


argParser = (
    ArgParser()
    .add("code", nargs="?")
    .add("--dev", action="store_true")
    .add("--test", action="store_true")
)

if __name__ == "__main__":
    main()
