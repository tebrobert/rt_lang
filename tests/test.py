from tests.custom import *
from utils.fail import *
from utils.interval import *
from utils.tailrec import *


# def test_desugar(current_test_dir_reader):
#     code = read_code(current_test_dir_reader)
#     actual_desugared = desugar(code)
#     expected_desugared = read_desugared(current_test_dir_reader)
#     return rt_assert_equal(actual_desugared, expected_desugared)


# def test_tokenize(current_test_dir_reader):
#     desugared = read_desugared(current_test_dir_reader)
#     actual_tokenized = tokenize(desugared)
#     expected_tokenized = read_tokenized(current_test_dir_reader)
#     return rt_assert_equal(actual_tokenized, expected_tokenized)


@tailrec
def run_tests_rec(tests, results=[]):
    return match_list(
        case_empty=lambda: results,
        case_at_least_1=lambda current_test, rest_tests: rec(
            rest_tests, results + [rt_try(current_test)]
        )
    )(tests)


def run_custom_tests():
    results = run_tests_rec(custom_tests)
    fails = list(filter(is_fail, results))
    match_list(
        case_empty=lambda: None,
        case_at_least_1=lambda head, _tail: print("The first failure:", head)
    )(fails)
    print(f"PASSED {len(results) - len(fails)} of {len(results)}")


def run_deferred_tests():
    print(f"DEFERRED {len(deferred_tests)}")
    results = run_tests_rec(deferred_tests)
    successes = list(filter(is_success, results))
    match_list(
        case_empty=lambda: None,
        case_at_least_1=lambda _head, _tail: print("SOME DEFERRED TESTS PASSED!")
    )(successes)


def run_tests():
    run_custom_tests()
    run_deferred_tests()

