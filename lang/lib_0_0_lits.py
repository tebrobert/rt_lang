from utils.fail import *


def match_builtin_idf(
    case_input,
    case_print,
    case_flatmap,
    case_pure,
    case_plus,
    case_minus,
    case_multiply,
    case_str,
    case_true,
    case_false,
    case_eq_eq,
):
    return lambda idf: ({
        builtin_input: case_input,
        builtin_print: case_print,
        builtin_flatmap: case_flatmap,
        builtin_pure: case_pure,
        builtin_plus: case_plus,
        builtin_minus: case_minus,
        builtin_multiply: case_multiply,
        builtin_str: case_str,
        builtin_true: case_true,
        builtin_false: case_false,
        builtin_eq_eq: case_eq_eq,
    }
    .get(
        idf,
        lambda: fail(f"Value {idf} {type(idf)} is not a built-in identifier")
    ))()


builtin_Bint = "Bint"
builtin_Bool = "Bool"
builtin_Str = "Str"
builtin_Unit = "Unit"
builtin_List = "List"
builtin_RIO = "RIO"
builtin_Func = "Func"

builtin_input = "input"
builtin_print = "print"
builtin_flatmap = ">>="
builtin_bind = ">>"
builtin_map = "map"
builtin_pure = "pure"
builtin_plus = "+"
builtin_minus = "-"
builtin_multiply = "*"
builtin_str = "str"
builtin_true = "true"
builtin_false = "false"
builtin_eq_eq = "=="
