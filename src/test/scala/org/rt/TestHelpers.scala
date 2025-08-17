package org.rt

import org.rt.lang.RtLib_3_Parse.{Expr, ExprCall1, ExprIdf, ExprLambda1}

object TestHelpers {
  def exprAndThen(
    result: String,
    expr: Expr,
    exprNext: Expr,
  ) =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf(result),
          exprNext,
        )
      ),
      expr
    )
}
