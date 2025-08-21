package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase14 extends RtTestCase {
  val code_0 = s"""greeting = "Hi!"\n"""
    + s"""print(greeting)\n"""
    + s"""print("What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Dear ".+(name).+(", welcome!"))\n"""

  val tokens_1 = List(
    TokIdf("greeting"), TokEq, TokLitStr("Hi!"), TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
    TokIdf("print"), TokParenOpen, TokLitStr("What is your name?"), TokParenClose, TokEndl,
    TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("print"), TokParenOpen, TokLitStr("Dear "),
    TokDot, TokIdf("+"), TokParenOpen, TokIdf("name"), TokParenClose,
    TokDot, TokIdf("+"), TokParenOpen, TokLitStr(", welcome!"), TokParenClose, TokParenClose, TokEndl,
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
