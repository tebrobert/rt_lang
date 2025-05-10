package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase8 extends RtTestCase {
  val code_0 = "s <- input\n"
    + "print(s)\n"
    + "print(s)\n"

  val tokens_1 = List(
    TokenIdf("s"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenEndl,
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
                ExprIdf("_"),
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
