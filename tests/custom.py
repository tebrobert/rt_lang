from lang.lib_5_build import *
from utils.fail import *


def test_sync_typs():
    rt_assert_equal(
        concreted(T_Func(T_A, T_Unit), T_Str),
        T_Func(T_Str, T_Unit)
    )


def test_assignment():
    full_build("""msg = "hi"\nprint(msg)""")


def test_assignment_lambdas():
    full_build("""f1 <- pure(+("1"))\nprint("0".f1)""")
    full_build("""f1 = x => x.+("1")\nprint("0".f1)""")


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


custom_tests = [
    test_sync_typs,

    test_assignment,
    test_method_syntax_1,
    test_method_syntax_2,
]

deferred_tests = [
    test_assignment_lambdas,
]
