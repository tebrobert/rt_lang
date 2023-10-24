from utils.fail import *


def match_builtin_idf(
    case_input,
    case_print,
    case_flatmap,
    case_pure,
    case_plus,
):
    return lambda idf: ({
        builtin_input: case_input,
        builtin_print: case_print,
        builtin_flatmap: case_flatmap,
        builtin_pure: case_pure,
        builtin_plus: case_plus,
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
