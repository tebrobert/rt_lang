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
    full_build_py("""f1 = +("1")\nprint("0".f1)""")


def test_method_syntax():
    rt_assert(
        full_parse("a.+(b).+(c).+(d)") ==
        full_parse("+(d)(+(c)(+(b)(a)))")
    )
    rt_assert(
        full_parse("f0(r0)(l0).f1(r1)(l1).f2(r2)(l2)") ==
        full_parse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))")
    )


def deferred_test_operator_naming():
    eval(full_build_str_py("""<<<~~~>>> = "Hello"\nprint(<<<~~~>>>)"""))


def deferred_test_flatmap_input():
    full_typify("""p = print("b")\np""")


custom_tests = [
    test_sync_typs,
    test_sync_typs_with_unknown_f_type,
    test_assignment,
    test_assignment_lambdas_1,
    test_assignment_lambdas_2,
    test_method_syntax,
]

deferred_tests = [
    deferred_test_operator_naming,
    deferred_test_flatmap_input,
]
