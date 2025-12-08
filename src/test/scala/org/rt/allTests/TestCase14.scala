package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock, RuntimeMocks}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

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

  val linted_3 =
    lintedEqAndThen("greeting", LintedLit("Hi!", T_Str),
      lintedAndThen("_", lintedCalls(lintedPrint, LintedIdf("greeting", T_Str)),
        lintedAndThen("_", lintedCalls(lintedPrint, LintedLit("What is your name?", T_Str)),
          lintedAndThen("name", lintedInput,
            lintedCalls(lintedPrint, lintedChain(LintedLit("Dear ", T_Str), (lintedPlusStr, LintedIdf("name", T_Str)), (lintedPlusStr, LintedLit(", welcome!", T_Str)))),
          ),
        ),
      ),
    )

  val mb_mock_4 =
    List(
      RuntimeMocks(List(
        PrintMock("Hi!"),
        PrintMock("What is your name?"),
        InputMock("Tester"),
        PrintMock("Dear Tester, welcome!"),
      )),
      RuntimeMocks(List(
        PrintMock("Hi!"),
        PrintMock("What is your name?"),
        InputMock(""),
        PrintMock("Dear , welcome!"),
      )),
    )
}
