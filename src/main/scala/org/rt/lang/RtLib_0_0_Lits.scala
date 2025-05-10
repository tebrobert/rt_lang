package org.rt.lang

import org.rt.utils.RtFail.rtFail

object RtLib_0_0_Lits {
  val builtin_Bint = "Bint"
  val builtin_Bool = "Bool"
  val builtin_Str = "Str"
  val builtin_Unit = "Unit"
  val builtin_List = "List"
  val builtin_RIO = "RIO"
  val builtin_Func = "Func"

  val builtin_input = "input"
  val builtin_print = "print"
  val builtin_flatmap = ">>="
  val builtin_bind = ">>"
  val builtin_map = "map"
  val builtin_pure = "pure"
  val builtin_plus = "+"
  val builtin_minus = "-"
  val builtin_multiply = "*"
  val builtin_div = "/"
  val builtin_floor_div = "//"
  val builtin_mod = "%"
  val builtin_str = "str"
  val builtin_true = "true"
  val builtin_false = "false"
  val builtin_eq_eq = "=="
  val builtin_not_eq = "!="
  val builtin_gr = ">"
  val builtin_gr_eq = ">="
  val builtin_less = "<"
  val builtin_less_eq = "<="
  val builtin_not = "not"
  val builtin_and = "and"
  val builtin_or = "or"

  def match_builtin_idf[A](
    case_input: () => A,
    case_print: () => A,
    case_flatmap: () => A,
    case_pure: () => A,
    case_plus: () => A,
    case_minus: () => A,
    case_multiply: () => A,
    case_str: () => A,
    case_true: () => A,
    case_false: () => A,
    case_eq_eq: () => A,
  ): String => A =
    idf => Map(
    builtin_input -> case_input,
    builtin_print -> case_print,
    builtin_flatmap -> case_flatmap,
    builtin_pure -> case_pure,
    builtin_plus -> case_plus,
    builtin_minus -> case_minus,
    builtin_multiply -> case_multiply,
    builtin_str -> case_str,
    builtin_true -> case_true,
    builtin_false -> case_false,
    builtin_eq_eq -> case_eq_eq,
    ).getOrElse(
      idf,
      () => rtFail(s"Value $idf is not a built-in identifier")
    )()
}
