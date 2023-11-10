from utils.tailrec import *


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

from lang.lib_5_build import *
from utils.fail import *
from utils.read_file import *


def get_current_test_dir_reader(test_number):
    return lambda file_name: read_file(
        f"{path_tests_full}{test_number}/{file_name}",
    )


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


def test_parse(current_test_dir_reader):
    tokenized = eval(read_tokenized(current_test_dir_reader))
    actual_parsed = parse(tokenized)
    expected_parsed = read_parsed(current_test_dir_reader)
    return rt_assert_equal(actual_parsed, expected_parsed)


def test_typified(current_test_dir_reader, test_number):
    #parsed = eval(read_parsed(current_test_dir_reader))
    code = read_code(current_test_dir_reader)
    parsed = full_parse(code)
    actual_typified = typify(parsed)
    expected_typified = read_typified(current_test_dir_reader)
    return rt_assert_equal(actual_typified, expected_typified, test_number)


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
        test_typified(current_test_dir_reader, test_number)
        test_built(current_test_dir_reader, test_number)

    return lazy_full_test


def test_sync_typs():
    rt_assert_equal(
        concreted(T_Func(T_A, T_Unit), T_Str),
        T_Func(T_Str, T_Unit)
    )


def test_sync_typs_with_unknown_f_type():
    rt_assert_equal(
        concreted(T_A, T_Str),
        T_Func(T_Str, T_A)
    )


def test_assignment():
    full_build_py("""msg = "hi"\nprint(msg)""")


def test_assignment_lambdas_1():
    full_build_py("""f1 <- pure(+("1"))\nprint("0".f1)""")


def test_assignment_lambdas_2():
    full_build_py("""f1 = x => x.+("1")\nprint("0".f1)""")


def test_assignment_lambdas_3():
    full_build_py("""f1 = +("1")\nprint("0".f1)""")


def test_method_syntax_1():
    full_parse("a.b")


def test_method_syntax_2():
    rt_assert_equal(
        full_parse("a.+(b).+(c).+(d)"),
        full_parse("+(d)(+(c)(+(b)(a)))"),
    )


def test_method_syntax_3():
    rt_assert(
        full_parse("f0(r0)(l0).f1(r1)(l1).f2(r2)(l2)") ==
        full_parse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))")
    )


def test_operator_naming_1():
    full_build_py("""<<<~~~>>> = "Hello"\nprint(<<<~~~>>>)""")


def test_operator_naming_2():
    full_build_py(
        """|||+++||| = "Hi!"\n""" +
        """print(|||+++|||)\n""" +
        """~~~ = name => "Welcome, ".+(name).+("!")\n""" +
        """print("Joe".~~~)"""
    )


def test_flatmap_input_1():
    full_build_py(
        """p = print("b")\n""" +
        """p"""
    )


def test_flatmap_input_2():
    full_build_py(
        """doAskName = print("What your name?").>>=(_ => input)\n""" +
        """doGreet = name => "Hi, ".+(name).+("!").print\n""" +
        """doAskName.>>=(doGreet)\n"""
    )


def test_lines_reversed():
    current_test_dir_reader = get_current_test_dir_reader(11)
    code = read_code(current_test_dir_reader)
    tokens = tokenize(code)
    ext_tokens_reversed = list(reversed([TokenEndl()] + tokens))
    raw_lines_reversed = get_lines_reversed(ext_tokens_reversed)
    actual_lines_reversed = list(filter(len, raw_lines_reversed))

    expected_lines_reversed = [
        [TokenIdf("print"), TokenParenOpen(), TokenIdf("name"),
            TokenParenClose(),
        ],
        [TokenIdf("print"), TokenParenOpen(), TokenLitStr("Welcome, ..."),
            TokenParenClose(),
        ],
        [TokenIdf("name"), TokenLessMinus(), TokenIdf("input")],
        [TokenIdf("print"), TokenParenOpen(), TokenIdf("greeting"),
            TokenParenClose()], [TokenIdf("greeting"), TokenEq(),
            TokenLitStr("Hey! What is your name?"),
        ]
    ]
    rt_assert_equal(actual_lines_reversed, expected_lines_reversed)


def test_parse_sugared_1():
    current_test_dir_reader = get_current_test_dir_reader(7)
    code = read_code(current_test_dir_reader)
    full_parse(code)


def test_parse_sugared_2():
    current_test_dir_reader = get_current_test_dir_reader(2)
    code = read_code(current_test_dir_reader)
    full_parse(code)


def test_preparse_braced_1():
    rt_assert_equal(preparse_braced([]), [])
    rt_assert_equal(preparse_braced([TokenDot()]), [TokenDot()])
    rt_assert_equal(preparse_braced([TokenDot(), TokenEqGr()]),
        [TokenDot(), TokenEqGr()])


def test_preparse_braced_2():
    preparse_braced([TokenParenOpen(), TokenIdf("+"), TokenParenClose()])


def test_rt_assert_at_least_1():
    rt_assert_at_least_1([None])


def test_match_token_1():
    str_idf = "idf"
    str_otherwise = "otherwise"
    token_s = match_token(
        case_idf=lambda s: s,
        otherwise=lambda: str_otherwise,
    )(TokenIdf(str_idf))
    rt_assert_equal(token_s, str_idf)


def test_match_token_2():
    str_idf = "idf"
    str_otherwise = "otherwise"
    token_s = match_token(
        case_idf=lambda s: s,
        otherwise=lambda: str_otherwise,
    )(TokenLitStr(str_idf))
    rt_assert_equal(token_s, str_otherwise)


def test_new_preparse_call():
    exprs = [
        ExprIdf("f"),
        ExprBraced(ExprLitStr("x")),
        ExprBraced(ExprLitStr("y")),
    ]
    preparse_call(exprs)


def test_parse_with_preparse():
    full_build_py("""print("q")""")


def test_parse_with_preparse_2():
    full_build_py("""x <- input\nprint(x)""")


def test_parse_with_preparse_3():
    full_build_py("""f = print("x")\nf""")


def test_parse_with_preparse_4():
    tokens = [TokenIdf("f"), TokenParenOpen(), TokenLitStr("x"),
        TokenParenClose(), TokenParenOpen(), TokenLitStr("y"),
        TokenParenClose()]
    parse(tokens)
    full_parse("""f("y")""")
    full_parse("""print(+("y")("x"))""")


def test_integers():
    full_parse("""\n1\n""")


def test_integers_printing():
    full_build_py("""num = 1\nprint("The number is " + str(num))""")


def test_integers_plus():
    full_build_py("""num = 1 + 2\nprint("The number is " + str(num))""")


def test_typify_set():
    typify_set(ExprIdf("num"))


def test_match_list_10():
    match_list(
        case_at_least_1=lambda head, _tail: head,
        case_empty=lambda: 0,
    )([])


def test_match_list_2o():
    match_list(
        case_at_least_2=lambda head0, head1, tail1: head0 + head1,
        otherwise=lambda: 0,
    )([])


def test_unary_minus():
    full_typify("print(str(-2))")


def test_funcs_1():
    full_typify("""identity = (x => x)("")\nprint(identity)""")


def test_funcs_2():
    print(full_typify("""identity = (x => x)\nprint(identity(""))"""))


custom_tests = [
    full_test(1),
    full_test(2),
    full_test(3),
    full_test(4),
    full_test(5),
    full_test(6),
    full_test(7),
    full_test(8),
    full_test(9),
    full_test(10),
    full_test(11),
    full_test(12),
    full_test(13),
    full_test(14),
    test_sync_typs,
    test_sync_typs_with_unknown_f_type,
    test_lines_reversed,
    test_method_syntax_1,
    test_method_syntax_2,
    test_method_syntax_3,
    test_preparse_braced_1,
    test_preparse_braced_2,
    test_match_token_1,
    test_match_token_2,
    test_assignment,
    test_flatmap_input_1,
    test_operator_naming_1,
    test_parse_sugared_1,
    test_rt_assert_at_least_1,
    test_parse_with_preparse,
    test_parse_with_preparse_2,
    test_parse_with_preparse_3,
    test_new_preparse_call,
    test_parse_with_preparse_4,
    test_flatmap_input_2,
    test_operator_naming_2,
    test_parse_sugared_2,
    test_assignment_lambdas_1,
    test_assignment_lambdas_2,
    test_assignment_lambdas_3,
    test_integers,
    test_integers_printing,
    test_typify_set,
    test_integers_plus,
    test_match_list_10,
    test_match_list_2o,
    test_unary_minus,
    test_funcs_1,
]

deferred_tests = [
    test_funcs_2,
]

path_tests_full = "tests/full/"
