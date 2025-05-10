package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase14 extends RtTestCase {
  val code_0 = s"""greeting = "Hi!"\n"""
    + s"""print(greeting)\n"""
    + s"""print("What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Dear ".+(name).+(", welcome!"))\n"""

  val tokens_1 = List(
    TokenIdf("greeting"), TokenEq, TokenLitStr("Hi!"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("greeting"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("What is your name?"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("Dear "),
    TokenDot, TokenIdf("+"), TokenParenOpen, TokenIdf("name"), TokenParenClose,
    TokenDot, TokenIdf("+"), TokenParenOpen, TokenLitStr(", welcome!"), TokenParenClose, TokenParenClose, TokenEndl,
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
                                  ExprLitStr(", welcome!")
                                ),
                                ExprCall1(
                                  ExprCall1(
                                    ExprIdf("+"),
                                    ExprIdf("name")
                                  ),
                                  ExprLitStr("Dear ")
                                )
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
                    ExprLitStr("What is your name?")
                  )
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
        ExprLitStr("Hi!")
      )
    )
}
