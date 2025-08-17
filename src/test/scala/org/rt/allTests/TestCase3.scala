package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{LintedCall1, LintedIdf, LintedLambda1}

object TestCase3 extends RtTestCase {
  val code_0 = "(s => s)(s => s)(input)\n"

  val tokens_1 = List(
    TokParenOpen, TokIdf("s"), TokEqGr, TokIdf("s"), TokParenClose,
    TokParenOpen, TokIdf("s"), TokEqGr, TokIdf("s"), TokParenClose,
    TokParenOpen, TokIdf("input"), TokParenClose,
    TokEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprLambda1(ExprIdf("s"), ExprIdf("s")),
        ExprLambda1(ExprIdf("s"), ExprIdf("s")),
      ),
      ExprIdf("input"),
    )

  override val mb_linted_3 = Some(LintedCall1(
    LintedCall1(
      LintedLambda1(
        LintedIdf("s", T_Func(T_A0, T_A0)),
        LintedIdf("s", T_Func(T_RIO(T_Str), T_RIO(T_Str))),
        T_Func(T_Func(T_A0, T_A0), T_Func(T_RIO(T_Str), T_RIO(T_Str))),
      ),
      LintedLambda1(
        LintedIdf("s", T_A0),
        LintedIdf("s", T_A0),
        T_Func(T_A0, T_A0),
      ),
      T_Func(T_RIO(T_Str), T_RIO(T_Str)),
    ),
    LintedIdf("input", T_RIO(T_Str)),
    T_RIO(T_Str),
  ))
}
