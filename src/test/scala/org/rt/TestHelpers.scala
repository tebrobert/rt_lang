package org.rt

import org.rt.lang.RtLib_0_0_Lits.builtin_flatmap
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*
import org.rt.utils.RtFail.rt_assert_type_Typ1

object TestHelpers {
  val T_RIO_Unit = T_RIO(T_Unit)
  val T_RIO_Str = T_RIO(T_Str)
  val T_Str_To_RIO_Unit = T_Str tTo T_RIO_Unit
  val T_Str_To_RIO_Str = T_Str tTo T_RIO_Str

  def exprAndThen(
    result: String,
    expr: Expr,
    exprNext: Expr,
  ) =
    ExprCall1(
      ExprCall1(
        ExprIdf(builtin_flatmap),
        ExprLambda1(
          ExprIdf(result),
          exprNext,
        )
      ),
      expr,
    )

  def lintedAndThen(
    resName: String,
    linted: Linted,
    lintedNext: Linted,
  ) = {
    val resTyp = rt_assert_type_Typ1(linted.typ).t1 // todo - try better typing

    LintedCall1(
      LintedCall1(
        LintedIdf(
          ">>=",
          (resTyp tTo lintedNext.typ)
            tTo (T_RIO(resTyp) tTo lintedNext.typ)
        ),
        LintedLambda1(LintedIdf(resName, resTyp), lintedNext, resTyp tTo lintedNext.typ),
        T_RIO(resTyp) tTo lintedNext.typ,
      ),
      linted,
      lintedNext.typ,
    )
  }
}
