package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.InputMock
import org.rt.lang.RtLib_0_2_Builtins.{T_RIO, T_Str}
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.LintedIdf

object TestCase1_2 extends RtTestCase {
  val code_0 =
    """(input)"""

  val tokens_1 =
    List(
      TokParenOpen,
      TokIdf("input"),
      TokParenClose,
    )

  val expr_2 =
    ExprIdf("input")

  val linted_3 =
    LintedIdf("input", T_RIO(T_Str))

  override val mb_mock_4 =
    List(
      List(InputMock("")),
      List(InputMock("s")),
    )
}
