package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase12 extends RtTestCase {
  val code_0 = s"""greeting = "Hey! What is your name?"\n"""
    + s"""print(greeting)\n"""
    + s"""name <- input\n"""
    + s"""print(+(name)("Welcome, "))"""

  val tokens_1 = List(
    TokenIdf("greeting"), TokenEq, TokenLitStr("Hey! What is your name?"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("greeting"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("+"), TokenParenOpen, TokenIdf("name"), TokenParenClose,
    TokenParenOpen, TokenLitStr("Welcome, "), TokenParenClose, TokenParenClose,
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
                        ExprIdf("print"),
                        ExprCall1(
                          ExprCall1(
                            ExprIdf("+"),
                            ExprIdf("name")
                          ),
                          ExprLitStr("Welcome, ")
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
