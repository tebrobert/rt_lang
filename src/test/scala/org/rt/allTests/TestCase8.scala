package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_1_Types.Typ
import org.rt.lang.RtLib_0_2_Builtins.{T_RIO, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1}

object TestCase8 extends RtTestCase {
  val code_0 =
    """s <- input
      |print(s)
      |print(s)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("s"), TokLessMinus, TokIdf("input"), TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose, TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose, TokEndl,
    )

  val expr_2 =
    exprAndThen("s", ExprIdf("input"),
      exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("s")),
        ExprCall1(ExprIdf("print"), ExprIdf("s")),
      ),
    )

  override val mb_linted_3 =
    Some(
      lintedAndThen("s", LintedIdf("input", T_RIO_Str),
        lintedAndThen("_", LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("s", T_Str), T_RIO_Unit),
          LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("s", T_Str), T_RIO_Unit),
        )
      )
    )
}
