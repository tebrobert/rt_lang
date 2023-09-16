import argparse
import os
import sys
from utils.rt_assert import *
from lang.lib_6_run import *

"""
Steps:
...

Shortcuts:
...
"""


class ArgParser:
    def __init__(self):
        self.argParser = argparse.ArgumentParser()

    def add(self, x, **kwargs):
        new_parser = ArgParser()
        new_parser.argParser = self.argParser
        new_parser.argParser.add_argument(x, **kwargs)
        return new_parser

    def parse(self, arg_line):
        argv = [] if arg_line == "" else arg_line.split(" ")
        return self.argParser.parse_args(argv)


def read_file(file_path):
    with open(file_path) as f:
        return f.read()


def print_if(cond, value):
    if cond:
        print(value)


def print_header_if(cond, header):
    print_if(cond, f"{header}:")


def print_headered_if(cond):
    return lambda header: lambda value: (
        (print_header_if(cond, header), print_if(cond, f"{value}\n"))
    )


def do_mb_headered(action, value, mb_print_headered):
    return flattap(lambda: action(value), mb_print_headered)


def asserted_desugar(read_result, code):
    return rt_try_assert_equal(
        "desugared",
        read_result("2_desugared.rt.txt"), lambda: desugar(code)
    )


def asserted_lexx(read_result, desugared):
    return rt_try_assert_equal(
        "tokens",
        read_result("3_tokens.py.txt"), lambda: lexx(desugared)
    )


def asserted_parse(read_result, tokens):
    return rt_try_assert_equal(
        "expr",
        read_result("4_expr.py.txt"), lambda: parse(tokens)
    )


def asserted_sem(read_result, expr):
    return rt_try_assert_equal(
        "typed",
        read_result("5_typed.txt"), lambda: sem(expr)
    )


def asserted_show(read_result, typed):
    return rt_try_assert_equal(
        "shown",
        read_result("6_shown.py.txt"), lambda: show(typed)
    )


def run_tests():
    path_tests = "tests/"
    for currentElement in sorted(os.listdir(path_tests), key=int):
        path_current_test = f"{path_tests}{currentElement}/"

        def read_current_test_file(file_name):
            return read_file(f"{path_current_test}/{file_name}")

        print(currentElement, end=": ")

        try:
            code = read_current_test_file("1_code.rt.txt")
            desugared = asserted_desugar(read_current_test_file, code)
            tokens = asserted_lexx(read_current_test_file, desugared)
            expr = asserted_parse(read_current_test_file, tokens)
            typed = asserted_sem(read_current_test_file, expr)
            _shown = asserted_show(read_current_test_file, typed)
            print("PASSED")
        except AssertionError as e:
            print(f"FAILED: {e}")
        except Exception as e:
            print(f"FAILED: {e}")


def unsafe_run_code(code, dev):
    try:
        print_headered_if_dev = print_headered_if(dev)
        print_headered_if_dev("1_CODE")(code)
        desugared = do_mb_headered(desugar, code,
            print_headered_if_dev("2_DESUGARED")
        )
        tokens = do_mb_headered(lexx, desugared,
            print_headered_if_dev("3_TOKENS")
        )
        expr = do_mb_headered(parse, tokens, print_headered_if_dev("4_EXPR"))
        typed = do_mb_headered(sem, expr, print_headered_if_dev("5_TYPED"))
        shown = do_mb_headered(show, typed, print_headered_if_dev("6_SHOWN"))
        built = build(shown)
        print_header_if(dev, "7_RUNNING")
        unsafe_run_built(built)
    except Exception as e:
        print(e)


def main():
    args = argParser.parse(get_args_line())
    if args.test:
        run_tests()
    elif args.code is not None:
        unsafe_run_code(code=read_file(args.code), dev=args.dev)


def prompt_args():
    return input("Enter command line args: ")


def get_args_line():
    return (prompt_args() if len(sys.argv) == 1 else
            " ".join(sys.argv[1:])
            )


argParser = (ArgParser()
             .add("code", nargs="?")
             .add("--dev", action="store_true")
             .add("--test", action="store_true")
             )

if __name__ == "__main__":
    main()
