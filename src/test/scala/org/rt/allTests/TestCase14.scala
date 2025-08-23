package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*

object TestCase14 extends RtTestCase {
  val code_0 =
    """greeting = "Hi!"
      |print(greeting)
      |print("What is your name?")
      |name <- input
      |print("Dear ".+(name).+(", welcome!"))
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("greeting"), TokEq, TokLitStr("Hi!"), TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
      TokIdf("print"), TokParenOpen, TokLitStr("What is your name?"), TokParenClose, TokEndl,
      TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,

      TokIdf("print"), TokParenOpen, TokLitStr("Dear "),
      TokDot, TokIdf("+"), TokParenOpen, TokIdf("name"), TokParenClose,
      TokDot, TokIdf("+"), TokParenOpen, TokLitStr(", welcome!"),
      TokParenClose, TokParenClose, TokEndl,
    )

  val expr_2 =
    exprEqAndThen("greeting", ExprLitStr("Hi!"),
      exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("greeting")),
        exprAndThen("_", ExprCall1(ExprIdf("print"), ExprLitStr("What is your name?")),
          exprAndThen("name", ExprIdf("input"),
            ExprCall1(ExprIdf("print"), exprChain(ExprLitStr("Dear "), (ExprIdf("+"), ExprIdf("name")), (ExprIdf("+"), ExprLitStr(", welcome!")))),
          ),
        ),
      ),
    )
}
