package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase10 extends RtTestCase {
  val code_0 = s"""greeting <- pure("Hey! What is your name?")\n"""
    + s"""print(greeting)\n"""
    + s"""name <- input\n"""
    + s"""print("Welcome, ...")\n"""
    + s"""print(name)\n"""

  val tokens_1 = List(
    TokenIdf("greeting"), TokenLessMinus, TokenIdf("pure"), TokenParenOpen,
    TokenLitStr("Hey! What is your name?"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("greeting"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("Welcome, ..."), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("name"), TokenParenClose, TokenEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("greeting"),
          ExprCall1(
            ExprCall1(
              ExprIdf(">>="),
              ExprLambda1(
                ExprIdf("_"),
                ExprCall1(
                  ExprCall1(
                    ExprIdf(">>="),
                    ExprLambda1(
                      ExprIdf("name"),
                      ExprCall1(
                        ExprCall1(
                          ExprIdf(">>="),
                          ExprLambda1(
                            ExprIdf("_"),
                            ExprCall1(
                              ExprIdf("print"),
                              ExprIdf("name")
                            )
                          )
                        ),
                        ExprCall1(
                          ExprIdf("print"),
                          ExprLitStr("Welcome, ...")
                        )
                      )
                    )
                  ),
                  ExprIdf("input")
                )
              )
            ),
            ExprCall1(
              ExprIdf("print"),
              ExprIdf("greeting")
            )
          )
        )
      ),
      ExprCall1(
        ExprIdf("pure"),
        ExprLitStr("Hey! What is your name?")
      )
    )
}
