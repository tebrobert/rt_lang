package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.exprAndThen
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase9 extends RtTestCase {
  val code_0 =
    """print("Hey! What is your name?")
      |name <- input
      |print("Welcome, ...")
      |print(name)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("print"), TokParenOpen, TokLitStr("Hey! What is your name?"), TokParenClose, TokEndl,
      TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,
      TokIdf("print"), TokParenOpen, TokLitStr("Welcome, ..."), TokParenClose, TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("name"), TokParenClose, TokEndl,
    )

  val expr_2 =
    exprAndThen("_", ExprCall1(ExprIdf("print"), ExprLitStr("Hey! What is your name?")),
      exprAndThen("name", ExprIdf("input"),
        exprAndThen("_", ExprCall1(ExprIdf("print"), ExprLitStr("Welcome, ...")),
          ExprCall1(ExprIdf("print"), ExprIdf("name"))
        )
      )
    )
}
