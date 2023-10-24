from tests.custom import *
from utils.fail import *
from utils.interval import *
from utils.read_file import *
from utils.tailrec import *


def read_code(current_test_dir_reader):
    return current_test_dir_reader("1_code.rt.txt")


def read_desugared(current_test_dir_reader):
    return current_test_dir_reader("2_desugared.rt.txt")


def read_tokenized(current_test_dir_reader):
    return current_test_dir_reader("3_tokens.py.txt")


def read_parsed(current_test_dir_reader):
    return current_test_dir_reader("4_expr.py.txt")


def read_typified(current_test_dir_reader):
    return current_test_dir_reader("5_typed.txt")


def read_built(current_test_dir_reader):
    return current_test_dir_reader("6_shown.py.txt")


def test_desugar(current_test_dir_reader):
    code = read_code(current_test_dir_reader)
    actual_desugared = desugar(code)
    expected_desugared = read_desugared(current_test_dir_reader)
    return rt_assert_equal("desugar", actual_desugared)(expected_desugared)


def test_tokenize(current_test_dir_reader):
    desugared = read_desugared(current_test_dir_reader)
    actual_tokenized = tokenize(desugared)
    expected_tokenized = read_tokenized(current_test_dir_reader)
    return rt_assert_equal("tokenize", actual_tokenized)(expected_tokenized)


def test_parse(current_test_dir_reader):
    tokenized = eval(read_tokenized(current_test_dir_reader))
    actual_parsed = parse(tokenized)
    expected_parsed = read_parsed(current_test_dir_reader)
    return rt_assert_equal("parse", actual_parsed)(expected_parsed)


def test_typified(current_test_dir_reader):
    #parsed = eval(read_parsed(current_test_dir_reader))
    code = read_code(current_test_dir_reader)
    parsed = full_parse(code)
    actual_typified = typify(parsed)
    expected_typified = read_typified(current_test_dir_reader)
    return rt_assert_equal("typify", actual_typified)(expected_typified)


def test_built(current_test_dir_reader):
    #typified = read_typified(current_test_dir_reader)
    code = read_code(current_test_dir_reader)
    typified = full_typify(code)
    actual_built = build(typified)
    expected_built = read_built(current_test_dir_reader)
    return rt_assert_equal("build", actual_built)(expected_built)


def get_current_test_dir_reader(test_number):
    return lambda file_name: read_file(
        f"{path_tests_full}{test_number}/{file_name}",
    )


def full_test(test_number):
    def lazy_full_test():
        current_test_dir_reader = get_current_test_dir_reader(test_number)
        test_desugar(current_test_dir_reader)
        test_tokenize(current_test_dir_reader)
        test_parse(current_test_dir_reader)
        test_typified(current_test_dir_reader)
        test_built(current_test_dir_reader)

    return lazy_full_test


@tailrec
def run_tests_rec(tests, results=[]):
    return match_list(
        case_empty=lambda: results,
        case_nonempty=lambda current_test, rest_tests: rec(
            rest_tests, results + [rt_try(current_test)]
        )
    )(tests)


def run_custom_tests():
    results = run_tests_rec(
        list(map(full_test, interval(1, 14)))
        + custom_tests
    )
    fails = list(filter(is_fail, results))
    match_list(
        case_empty=lambda: None,
        case_nonempty=lambda head, _tail: print("The first failure:", head)
    )(fails)
    print(f"PASSED {len(results) - len(fails)} of {len(results)}")


def run_deferred_tests():
    print(f"DEFERRED {len(deferred_tests)}")
    results = run_tests_rec(deferred_tests)
    successes = list(filter(is_success, results))
    match_list(
        case_empty=lambda: None,
        case_nonempty=lambda _head, _tail: print("SOME DEFERRED TESTS PASSED!")
    )(successes)


def run_tests():
    run_custom_tests()
    run_deferred_tests()


path_tests_full = "tests/full/"
