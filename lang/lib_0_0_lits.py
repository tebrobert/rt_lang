from utils.fail import *


def match_builtin_idf(
    lazy_for_input,
    lazy_for_print,
    lazy_for_flatmap,
    lazy_for_pure,
    lazy_for_plus,
):
    return lambda idf: ({
        builtin_input: lazy_for_input,
        builtin_print: lazy_for_print,
        builtin_flatmap: lazy_for_flatmap,
        builtin_pure: lazy_for_pure,
        builtin_plus: lazy_for_plus,
    }
    .get(
        idf,
        lambda: fail(f"Value {idf} {type(idf)} is not a built-in identifier")
    ))()


builtin_Int = "Int"
builtin_Str = "Str"
builtin_Unit = "Unit"
builtin_List = "List"
builtin_RIO = "RIO"
builtin_Func = "Func"

builtin_input = "input"
builtin_print = "print"
builtin_flatmap = ">>="
builtin_pure = "pure"
builtin_plus = "+"
