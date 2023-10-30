from lang.lib_5_build import *
from utils.fail import *


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
    rt_assert(
        full_parse("a.+(b).+(c).+(d)") ==
        full_parse("+(d)(+(c)(+(b)(a)))")
    )


def test_method_syntax_2():
    rt_assert(
        full_parse("f0(r0)(l0).f1(r1)(l1).f2(r2)(l2)") ==
        full_parse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))")
    )


def test_operator_naming_1():
    full_build_py("""<<<~~~>>> = "Hello"\nprint(<<<~~~>>>)""")


def test_operator_naming_2():
    full_build_py(
        """|||+++||| = "Hi!"\n"""+
        """print(|||+++|||)\n"""+
        """~~~ = name => "Welcome, ".+(name).+("!")\n"""+
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


custom_tests = [
    test_sync_typs,
    test_sync_typs_with_unknown_f_type,
    test_assignment,
    test_assignment_lambdas_1,
    test_assignment_lambdas_2,
    test_assignment_lambdas_3,
    test_method_syntax_1,
    test_method_syntax_2,
    test_flatmap_input_1,
    test_flatmap_input_2,
    test_operator_naming_1,
    test_operator_naming_2,
]

deferred_tests = [
]
