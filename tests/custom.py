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
    tokens = full_tokenize(code)
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
    rt_assert_equal(new_preparse_braced([]), [])
    rt_assert_equal(new_preparse_braced([TokenDot()]), [TokenDot()])
    rt_assert_equal(new_preparse_braced([TokenDot(), TokenEqGr()]), [TokenDot(), TokenEqGr()])


def test_preparse_braced_2():
    new_preparse_braced([TokenParenOpen(), TokenIdf("+"), TokenParenClose()])


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
    new_preparse_call(exprs)


def test_parse_with_preparse():
    full_build_py("""print("q")""")


def test_parse_with_preparse_2():
    full_build_py("""x <- input\nprint(x)""")


def test_parse_with_preparse_3():
    full_build_py("""f = print("x")\nf""")


def test_parse_with_preparse_4():
    tokens = [TokenIdf("f"), TokenParenOpen(), TokenLitStr("x"), TokenParenClose(), TokenParenOpen(), TokenLitStr("y"), TokenParenClose()]
    parse(tokens)
    return
    full_parse("""f("y")""")
    return
    full_parse("""print(+("y")("x"))""")


custom_tests = [
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
]*1 + [
]

deferred_tests = [
    test_assignment_lambdas_1,
    test_assignment_lambdas_2,
    test_assignment_lambdas_3,
    test_flatmap_input_2,
    test_operator_naming_2,
    test_parse_sugared_2,
]*0 + [
]*0

path_tests_full = "tests/full/"
