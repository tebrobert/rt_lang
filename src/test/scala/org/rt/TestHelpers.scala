package org.rt

import org.rt.lang.RtLib_0_0_Lits.builtin_flatmap
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*

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
    resultTuple: (String, Typ),
    linted: Linted,
    lintedNext: Linted,
  ) = {
    val lintedResult = LintedIdf.apply.tupled(resultTuple)

    LintedCall1(
      LintedCall1(
        LintedIdf(
          ">>=",
          (lintedResult.typ tTo lintedNext.typ)
            tTo (T_RIO(lintedResult.typ) tTo lintedNext.typ)
        ),
        LintedLambda1(lintedResult, lintedNext, lintedResult.typ tTo lintedNext.typ),
        T_RIO(lintedResult.typ) tTo lintedNext.typ,
      ),
      linted,
      lintedNext.typ,
    )
  }
}
