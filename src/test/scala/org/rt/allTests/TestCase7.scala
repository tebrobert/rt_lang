package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RunMock.{InputMock, PrintMock, RunMocks}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{LintedCall1, LintedIdf, LintedLambda1}

object TestCase7 extends RtTestCase {
  val code_0 = ""
    + "x <- input\n"
    + "y <- input\n"
    + "print(x)\n"
    + "print(y)"

  val tokens_1 = List(
    TokIdf("x"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("y"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("x"), TokParenClose, TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("y"), TokParenClose,
  )

  val expr_2 =
    exprAndThen("x", ExprIdf("input"),
      exprAndThen("y", ExprIdf("input"),
        exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("x")),
          ExprCall1(ExprIdf("print"), ExprIdf("y")),
        )
      )
    )

  val linted_3 =
    lintedAndThen("x", LintedIdf("input", T_RIO_Str),
      lintedAndThen("y", LintedIdf("input", T_RIO_Str),
        lintedAndThen("_", LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("x", T_Str), T_RIO_Unit),
          LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("y", T_Str), T_RIO_Unit),
        ),
      ),
    )

  override val mb_mock_4 =
    Some(
      //List(InputMock("x"), InputMock("y"), PrintMock("x"), PrintMock("y"))
      RunMocks(List(InputMock(""), InputMock(""), PrintMock(""), PrintMock("")))
    )
}
