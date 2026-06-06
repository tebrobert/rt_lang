package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

object TestCase15 extends RtTestCase {
  val code_0 =
    """nameRequest = "What is your name?"
      |print(nameRequest)
      |name <- input
      |print("Dear " + name + ", welcome!")
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("nameRequest"),
      TokEq,
      TokLitStr("What is your name?"),
      TokEndl,
      TokIdf("print"),
      TokParenOpen,
      TokIdf("nameRequest"),
      TokParenClose,
      TokEndl,
      TokIdf("name"),
      TokLessMinus,
      TokIdf("input"),
      TokEndl,
      TokIdf("print"),
      TokParenOpen,
      TokLitStr("Dear "),
      TokIdf("+"),
      TokIdf("name"),
      TokIdf("+"),
      TokLitStr(", welcome!"),
      TokParenClose,
      TokEndl,
    )

  val expr_2 =
    exprEqAndThen(
      "nameRequest",
      ExprLitStr("What is your name?"),
      exprAndThen(
        "_",
        ExprCall1(ExprIdf("print"), ExprIdf("nameRequest")),
        exprAndThen(
          "name",
          ExprIdf("input"),
          ExprCall1(
            ExprIdf("print"),
            exprChain(
              ExprLitStr("Dear "),
              (ExprIdf("+"), ExprIdf("name")),
              (ExprIdf("+"), ExprLitStr(", welcome!")),
            ),
          ),
        ),
      ),
    )

  val linted_3 =
    lintedEqAndThen(
      "nameRequest",
      LintedLit("What is your name?", T_Str),
      lintedAndThen(
        "_",
        lintedCalls(lintedPrint, LintedIdf("nameRequest", T_Str)),
        lintedAndThen(
          "name",
          lintedInput,
          lintedCalls(
            lintedPrint,
            lintedChain(
              LintedLit("Dear ", T_Str),
              (lintedPlusStr, LintedIdf("name", T_Str)),
              (lintedPlusStr, LintedLit(", welcome!", T_Str)),
            ),
          ),
        ),
      ),
    )

  override val mb_mock_4 =
    List(
      List(
        PrintMock("What is your name?"),
        InputMock("Tester"),
        PrintMock("Dear Tester, welcome!"),
      ),
      List(
        PrintMock("What is your name?"),
        InputMock(""),
        PrintMock("Dear , welcome!"),
      ),
    )
}
