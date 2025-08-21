package org.rt

import org.rt.lang.RtLib_0_0_Lits.builtin_flatmap
import org.rt.lang.RtLib_0_2_Builtins.{T_RIO, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_3_Parse.{Expr, ExprCall1, ExprIdf, ExprLambda1}

object TestHelpers {
  val T_RIO_Unit = T_RIO(T_Unit)
  val T_RIO_Str = T_RIO(T_Str)
  val T_Str_To_RIO_Unit = T_Str tTo T_RIO_Unit

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
}
