package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

object TestCase9 extends RtTestCase {
  val code_0 =
    """print("Hey! What is your name?")
      |name <- input
      |print("Welcome, ...")
      |print(name)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("print"),
      TokParenOpen,
      TokLitStr("Hey! What is your name?"),
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
    exprAndThen(
      "_",
      ExprCall1(ExprIdf("print"), ExprLitStr("Hey! What is your name?")),
      exprAndThen(
        "name",
        ExprIdf("input"),
        exprAndThen(
          "_",
          ExprCall1(ExprIdf("print"), ExprLitStr("Welcome, ...")),
          ExprCall1(ExprIdf("print"), ExprIdf("name")),
        ),
      ),
    )

  val linted_3 =
    lintedAndThen(
      "_",
      LintedCall1(
        LintedIdf("print", T_Str_To_RIO_Unit),
        LintedLit("Hey! What is your name?", T_Str),
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
