package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase9 extends RtTestCase {
  val code_0 = ""
    + s"""print("Hey! What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Welcome, ...")\n"""
    + s"""print(name)\n"""

  val tokens_1 = List(
    TokIdf("print"), TokParenOpen,
    TokLitStr("Hey! What is your name?"), TokParenClose, TokEndl,
    TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("print"), TokParenOpen, TokLitStr("Welcome, ..."), TokParenClose, TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("name"), TokParenClose, TokEndl,
  )

  val expr_2 =
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
        ExprLitStr("Hey! What is your name?")
      )
    )
}
