package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import lang.RtLib_3_Parse.*
import org.rt.RtTestCase

object TestCase6 extends RtTestCase {
  val code_0 = ">>=(s => >>=(u => print(s))(print(s)))(input)\n"

  val tokens_1 = List(
    TokenIdf(">>="),
    TokenParenOpen, TokenIdf("s"), TokenEqGr,
    TokenIdf(">>="),
    TokenParenOpen, TokenIdf("u"), TokenEqGr, TokenIdf("print"),
    TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenParenClose,
    TokenParenOpen, TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenParenClose,
    TokenParenClose, TokenParenOpen, TokenIdf("input"), TokenParenClose, TokenEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("s"),
          ExprCall1(
            ExprCall1(
              ExprIdf(">>="),
              ExprLambda1(
                ExprIdf("u"),
                ExprCall1(
                  ExprIdf("print"),
                  ExprIdf("s")
                )
              )
            ),
            ExprCall1(
              ExprIdf("print"),
              ExprIdf("s")
            )
          )
        )
      ),
      ExprIdf("input")
    )
}
