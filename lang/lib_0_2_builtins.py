from lang.lib_0_1_types import *

T_Int = Type0(builtin_Int)
T_Str = Type0(builtin_Str)
T_Unit = Type0(builtin_Unit)
T_List = lambda t1: Type1(builtin_List, t1)
T_RIO = lambda t1: Type1(builtin_RIO, t1)
T_Func = lambda t1, t2: Type2(builtin_Func, t1, t2)

T_A = Unknown0("A")
T_B = Unknown0("B")

types = {
    builtin_Int: T_Int,
    builtin_Str: T_Str,
    builtin_Unit: T_Unit,
    builtin_List: T_List,
    builtin_RIO: T_RIO,
    builtin_Func: T_Func,
}

idf_to_type = {
    builtin_input: T_RIO(T_Str),
    builtin_print: T_Func(T_Str, T_RIO(T_Unit)),
    builtin_flatmap: T_Func(
        T_Func(T_A, T_RIO(T_B)),
        T_Func(T_RIO(T_A), T_RIO(T_B))
    ),
    builtin_pure: T_Func(T_A, T_RIO(T_A))
}
