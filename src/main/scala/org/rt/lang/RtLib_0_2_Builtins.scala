package org.rt.lang

import RtLib_0_0_Lits.*
import RtLib_0_1_Types.*

object RtLib_0_2_Builtins {
  val T_Bint = Typ0(builtin_Bint)
  val T_Str = Typ0(builtin_Str)
  val T_Unit = Typ0(builtin_Unit)
  val T_Bool = Typ0(builtin_Bool)
  val T_List = (t1: Typ) => Typ1(builtin_List, t1)
  val T_RIO = (t1: Typ) => Typ1(builtin_RIO, t1)
  private val T_Func = (from: Typ, to: Typ) => Typ2(builtin_Func, from, to)

  val T_A0 = Unk0(0)
  val T_A1 = Unk0(1)

  extension (typ: Typ) {
    def tTo(resultTyp: Typ) =
      T_Func(typ, resultTyp)
  }

  val idf_to_typ =
    Map[String, Set[Typ]](
      builtin_input -> Set(T_RIO(T_Str)),
      builtin_print -> Set(T_Str tTo T_RIO(T_Unit)),
      builtin_flatmap ->
        Set((T_A0 tTo T_RIO(T_A1)) tTo (T_RIO(T_A0) tTo T_RIO(T_A1))),
      builtin_pure -> Set(T_A0 tTo T_RIO(T_A0)),
      builtin_plus ->
        Set(
          T_Str tTo (T_Str tTo T_Str),
          T_Bint tTo (T_Bint tTo T_Bint),
        ),
      builtin_minus ->
        Set(
          T_Bint tTo (T_Bint tTo T_Bint),
          T_Bint tTo T_Bint,
        ),
      builtin_multiply -> Set(T_Bint tTo (T_Bint tTo T_Bint)),
      builtin_str ->
        Set(
          T_Bint tTo T_Str,
          T_Bool tTo T_Str,
        ),
      builtin_true -> Set(T_Bool),
      builtin_false -> Set(T_Bool),
      builtin_eq_eq ->
        Set(
          T_Str tTo (T_Str tTo T_Bool),
          T_Bint tTo (T_Bint tTo T_Bool),
          T_Bool tTo (T_Bool tTo T_Bool),
        )
    )
}
