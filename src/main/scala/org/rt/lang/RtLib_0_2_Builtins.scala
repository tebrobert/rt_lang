package org.rt.lang

import RtLib_0_0_Lits._
import RtLib_0_1_Types._

object RtLib_0_2_Builtins {
    val T_Bint = Typ0(builtin_Bint)
    val T_Str = Typ0(builtin_Str)
    val T_Unit = Typ0(builtin_Unit)
    val T_Bool = Typ0(builtin_Bool)

    def T_List(t1: Typ) = Typ1(builtin_List, t1)

    def T_RIO(t1: Typ) = Typ1(builtin_RIO, t1)

    def T_Func(t1: Typ, t2: Typ) = Typ2(builtin_Func, t1, t2)

    val T_A0 = Unk0(0)
    val T_A1 = Unk0(1)

    val types = Map(
        builtin_Bint -> T_Bint,
        builtin_Bool -> T_Bool,
        builtin_Str -> T_Str,
        builtin_Unit -> T_Unit,
        builtin_List -> T_List,
        builtin_RIO -> T_RIO,
        builtin_Func -> T_Func,
    )

    val idf_to_typ = Map(
        builtin_input -> Set(T_RIO(T_Str)),
        builtin_print -> Set(T_Func(T_Str, T_RIO(T_Unit))),
        builtin_flatmap -> Set(T_Func(
            T_Func(T_A0, T_RIO(T_A1)),
            T_Func(T_RIO(T_A0), T_RIO(T_A1))
        )),
        builtin_pure -> Set(T_Func(T_A0, T_RIO(T_A0))),
        builtin_plus -> Set(
            T_Func(T_Str, T_Func(T_Str, T_Str)),
            T_Func(T_Bint, T_Func(T_Bint, T_Bint)),
        ),
        builtin_minus -> Set(
            T_Func(T_Bint, T_Func(T_Bint, T_Bint)),
            T_Func(T_Bint, T_Bint),
        ),
        builtin_multiply -> Set(T_Func(T_Bint, T_Func(T_Bint, T_Bint))),
        builtin_str -> Set(
            T_Func(T_Bint, T_Str),
            T_Func(T_Bool, T_Str),
        ),
        builtin_true -> Set(T_Bool),
        builtin_false -> Set(T_Bool),
        builtin_eq_eq -> Set(
            T_Func(T_Str, T_Func(T_Str, T_Bool)),
            T_Func(T_Bint, T_Func(T_Bint, T_Bool)),
            T_Func(T_Bool, T_Func(T_Bool, T_Bool)),
        )
    )
}
