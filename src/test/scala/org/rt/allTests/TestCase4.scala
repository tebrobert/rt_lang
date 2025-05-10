package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase4 extends RtTestCase {
  val code_0 = "(s => s)((s => s)(input))\n"

  val tokens_1 = List(
    TokenParenOpen, TokenIdf("s"), TokenEqGr, TokenIdf("s"), TokenParenClose,
    TokenParenOpen,
    TokenParenOpen, TokenIdf("s"), TokenEqGr, TokenIdf("s"), TokenParenClose,
    TokenParenOpen, TokenIdf("input"), TokenParenClose,
    TokenParenClose, TokenEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprLambda1(ExprIdf("s"), ExprIdf("s")),
      ExprCall1(
        ExprLambda1(ExprIdf("s"), ExprIdf("s")),
        ExprIdf("input")
      )
    )
}
