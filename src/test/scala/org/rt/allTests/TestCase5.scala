package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase5 extends RtTestCase {
  val code_0 = ">>=(s => print(s))(input)\n"

  val tokens_1 = List(
    TokenIdf(">>="),
    TokenParenOpen,
    TokenIdf("s"), TokenEqGr, TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose,
    TokenParenClose,
    TokenParenOpen, TokenIdf("input"), TokenParenClose,
    TokenEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("s"),
          ExprCall1(ExprIdf("print"), ExprIdf("s")),
        )
      ),
      ExprIdf("input")
    )
}
