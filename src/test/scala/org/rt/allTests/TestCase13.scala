package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase13 extends RtTestCase {
  val code_0 = s"""greeting = "Hey! What is your name?"\n"""
    + s"""print(greeting)\n"""
    + s"""name <- input\n"""
    + s"""result = +(name)("Welcome, ")\n"""
    + s"""print(result)"""

  val tokens_1 = List(
    TokIdf("greeting"), TokEq, TokLitStr("Hey! What is your name?"), TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
    TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("result"), TokEq, TokIdf("+"),
    TokParenOpen, TokIdf("name"), TokParenClose,
    TokParenOpen, TokLitStr("Welcome, "), TokParenClose, TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("result"), TokParenClose,
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
                            ExprIdf("result"),
                            ExprCall1(
                              ExprIdf("print"),
                              ExprIdf("result")
                            )
                          )
                        ),
                        ExprCall1(
                          ExprIdf("pure"),
                          ExprCall1(
                            ExprCall1(
                              ExprIdf("+"),
                              ExprIdf("name")
                            ),
                            ExprLitStr("Welcome, ")
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
