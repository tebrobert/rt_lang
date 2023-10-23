from os import listdir
from lang.lib_6_run import *
from utils.arg_parser import *
from utils.print_if import *
from utils.read_file import *

"""
Steps:
...

Shortcuts:
...
"""


def asserted_desugar(read_result, code):
    return rt_try_assert_equal("desugared",
        read_result("2_desugared.rt.txt"), lambda: desugar(code)
    )


def asserted_tokenize(read_result, desugared):
    return rt_try_assert_equal("tokens",
        read_result("3_tokens.py.txt"), lambda: tokenize(desugared)
    )


def asserted_parse(read_result, tokens):
    return rt_try_assert_equal("expr",
        read_result("4_expr.py.txt"), lambda: parse(tokens)
    )


def asserted_sem(read_result, expr):
    return rt_try_assert_equal("typed",
        read_result("5_typed.txt"), lambda: sem(expr)
    )


def asserted_show(read_result, typed):
    shown = rt_try(lambda: show(typed))
    rt_assert_equal("shown", read_result("6_shown.py.txt"))(shown)


def read_test_file(current_element):
    def read_current_test_file(file_name):
        return read_file(f"{path_tests}{current_element}/{file_name}")

    return read_current_test_file


def run_tests():
    for current_element in sorted(listdir(path_tests), key=int):
        print(current_element, end=": ")
        read_current_test_file = read_test_file(current_element)
        code = read_current_test_file("1_code.rt.txt")
        try:
            desugared = asserted_desugar(read_current_test_file, code)
            tokens = asserted_tokenize(read_current_test_file, desugared)
            expr = asserted_parse(read_current_test_file, tokens)
            typed = asserted_sem(read_current_test_file, expr)
            asserted_show(read_current_test_file, typed)
            print("PASSED")
        except AssertionError as e:
            print(f"FAILED: {e}")


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
        typed = run(sem, expr, "5_TYPED")
        shown = run(show, typed, "6_SHOWN")
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

path_tests = "tests/full/"

if __name__ == "__main__":
    main()
