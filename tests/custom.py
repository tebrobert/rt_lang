from lang.lib_5_build import *
from utils.fail import *
from utils.tailrec import *


def test_assign_lambdas_1():
    full_build("""f1 <- pure(+("1"))\nprint("0".f1)""")


def test_assign_lambdas_2():
    full_build("""f1 = x => x.+("1")\nprint("0".f1)""")


@tailrec
def run_custom_tests_rec(tests, results=[]):
    return match_list(
        case_empty=lambda: results,
        case_nonempty=lambda current_test, rest_tests: rec(
            rest_tests, results + [rt_try(current_test)]
        )
    )(tests)


def run_custom_tests():
    results = run_custom_tests_rec([
        test_assign_lambdas_1,
        test_assign_lambdas_2,
    ])
    fails = list(filter(is_fail, results))
    print(f"PASSED {len(results) - len(fails)} of {len(results)}")
    match_list(
        case_empty=lambda: None,
        case_nonempty=lambda head, _tail: print(head)
    )(fails)
