package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import lang.RtLib_3_Parse.*
import org.rt.RtTestCase

object TestCase7 extends RtTestCase {
  val code_0 = "x <- input"
    + "\ny <- input"
    + "\nprint(x)"
    + "\nprint(y)"

  val tokens_1 = List(
    TokenIdf("x"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("y"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("x"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("y"), TokenParenClose,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("x"),
          ExprCall1(
            ExprCall1(
              ExprIdf(">>="),
              ExprLambda1(
                ExprIdf("y"),
                ExprCall1(
                  ExprCall1(
                    ExprIdf(">>="),
                    ExprLambda1(
                      ExprIdf("_"),
                      ExprCall1(
                        ExprIdf("print"),
                        ExprIdf("y")
                      )
                    )
                  ),
                  ExprCall1(
                    ExprIdf("print"),
                    ExprIdf("x")
                  )
                )
              )
            ),
            ExprIdf("input")
          )
        )
      ),
      ExprIdf("input")
    )
}
