package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

object TestCase11 extends RtTestCase {
  val code_0 =
    """greeting = "Hey! What is your name?"
      |print(greeting)
      |name <- input
      |print("Welcome, ...")
      |print(name)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("greeting"),
      TokEq,
      TokLitStr("Hey! What is your name?"),
      TokEndl,
      TokIdf("print"),
      TokParenOpen,
      TokIdf("greeting"),
      TokParenClose,
      TokEndl,
      TokIdf("name"),
      TokLessMinus,
      TokIdf("input"),
      TokEndl,
      TokIdf("print"),
      TokParenOpen,
      TokLitStr("Welcome, ..."),
      TokParenClose,
      TokEndl,
      TokIdf("print"),
      TokParenOpen,
      TokIdf("name"),
      TokParenClose,
      TokEndl,
    )

  val expr_2 =
    exprEqAndThen(
      "greeting",
      ExprLitStr("Hey! What is your name?"),
      exprAndThen(
        "_",
        ExprCall1(ExprIdf("print"), ExprIdf("greeting")),
        exprAndThen(
          "name",
          ExprIdf("input"),
          exprAndThen(
            "_",
            ExprCall1(ExprIdf("print"), ExprLitStr("Welcome, ...")),
            ExprCall1(ExprIdf("print"), ExprIdf("name")),
          ),
        ),
      ),
    )

  val linted_3 =
    lintedEqAndThen(
      "greeting",
      LintedLit("Hey! What is your name?", T_Str),
      lintedAndThen(
        "_",
        LintedCall1(
          LintedIdf("print", T_Str_To_RIO_Unit),
          LintedIdf("greeting", T_Str),
          T_RIO_Unit,
        ),
        lintedAndThen(
          "name",
          LintedIdf("input", T_RIO_Str),
          lintedAndThen(
            "_",
            LintedCall1(
              LintedIdf("print", T_Str_To_RIO_Unit),
              LintedLit("Welcome, ...", T_Str),
              T_RIO_Unit,
            ),
            LintedCall1(
              LintedIdf("print", T_Str_To_RIO_Unit),
              LintedIdf("name", T_Str),
              T_RIO_Unit,
            ),
          ),
        ),
      ),
    )

  val mb_mock_4 =
    List(
      List(
        PrintMock("Hey! What is your name?"),
        InputMock("Tester"),
        PrintMock("Welcome, ..."),
        PrintMock("Tester"),
      ),
      List(
        PrintMock("Hey! What is your name?"),
        InputMock(""),
        PrintMock("Welcome, ..."),
        PrintMock(""),
      ),
    )
}
