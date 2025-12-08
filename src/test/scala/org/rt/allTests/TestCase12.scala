package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock, RuntimeMocks}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

object TestCase12 extends RtTestCase {
  val code_0 =
    """greeting = "Hey! What is your name?"
      |print(greeting)
      |name <- input
      |print(+(name)("Welcome, "))""".stripMargin

  val tokens_1 =
    List(
      TokIdf("greeting"), TokEq, TokLitStr("Hey! What is your name?"), TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
      TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,

      TokIdf("print"), TokParenOpen, TokIdf("+"), TokParenOpen,
      TokIdf("name"), TokParenClose, TokParenOpen, TokLitStr("Welcome, "),
      TokParenClose, TokParenClose,
    )

  val expr_2 =
    exprEqAndThen("greeting", ExprLitStr("Hey! What is your name?"),
      exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("greeting")),
        exprAndThen("name", ExprIdf("input"),
          ExprCall1(ExprIdf("print"), exprCalls(ExprIdf("+"), ExprIdf("name"), ExprLitStr("Welcome, "))),
        ),
      ),
    )

  val linted_3 =
    lintedEqAndThen("greeting", LintedLit("Hey! What is your name?", T_Str),
      lintedAndThen("_", LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("greeting", T_Str), T_RIO_Unit),
        lintedAndThen("name", LintedIdf("input", T_RIO_Str),
          LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), lintedCalls(LintedIdf("+", T_Str tTo (T_Str tTo T_Str)), LintedIdf("name", T_Str), LintedLit("Welcome, ", T_Str)), T_RIO_Unit),
        ),
      ),
    )

  val mb_mock_4 =
    List(
      RuntimeMocks(List(
        PrintMock("Hey! What is your name?"),
        InputMock("Tester"),
        PrintMock("Welcome, Tester"),
      )),
      RuntimeMocks(List(
        PrintMock("Hey! What is your name?"),
        InputMock(""),
        PrintMock("Welcome, "),
      )),
    )
}
