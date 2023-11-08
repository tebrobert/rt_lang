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


def test_parse(current_test_dir_reader):
    tokenized = eval(read_tokenized(current_test_dir_reader))
    actual_parsed = parse(tokenized)
    expected_parsed = read_parsed(current_test_dir_reader)
    return rt_assert_equal(actual_parsed, expected_parsed)


def test_typified(current_test_dir_reader):
    #parsed = eval(read_parsed(current_test_dir_reader))
    code = read_code(current_test_dir_reader)
    parsed = full_parse(code)
    actual_typified = typify(parsed)
    expected_typified = read_typified(current_test_dir_reader)
    return rt_assert_equal(actual_typified, expected_typified)


def test_built(current_test_dir_reader, test_number):
    #typified = read_typified(current_test_dir_reader)
    code = read_code(current_test_dir_reader)
    typified = full_typify(code)
    actual_built = build_str_py(typified)
    expected_built = read_built(current_test_dir_reader)
    return rt_assert_equal(actual_built, expected_built, test_number)


def full_test(test_number):
    def lazy_full_test():
        current_test_dir_reader = get_current_test_dir_reader(test_number)
        #test_desugar(current_test_dir_reader)
        #test_tokenize(current_test_dir_reader)
        test_parse(current_test_dir_reader)
        test_typified(current_test_dir_reader)
        test_built(current_test_dir_reader, test_number)

    return lazy_full_test


@tailrec
def run_tests_rec(tests, results=[]):
    return match_list(
        case_empty=lambda: results,
        case_at_least_1=lambda current_test, rest_tests: rec(
            rest_tests, results + [rt_try(current_test)]
        )
    )(tests)


def run_custom_tests():
    results = run_tests_rec([]
        #+ list(map(full_test, interval(1, 14)))
        + custom_tests
    )
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

