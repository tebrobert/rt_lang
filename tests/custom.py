from lang.lib_5_build import *
from utils.fail import *


def method_syntax_1():
    rt_assert(
        full_parse("a.+(b).+(c).+(d)") ==
        full_parse("+(d)(+(c)(+(b)(a)))")
    )


def method_syntax_2():
    rt_assert(
        full_parse("f0(r0)(l0).f1(r1)(l1).f2(r2)(l2)") ==
        full_parse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))")
    )


def test_assign_lambdas_1():
    full_build("""f1 <- pure(+("1"))\nprint("0".f1)""")


def test_assign_lambdas_2():
    full_build("""f1 = x => x.+("1")\nprint("0".f1)""")


custom_tests = [
    method_syntax_1,
    method_syntax_2,
    test_assign_lambdas_1,
    test_assign_lambdas_2,
]
