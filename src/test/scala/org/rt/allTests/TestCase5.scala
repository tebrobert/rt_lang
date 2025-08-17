package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{LintedCall1, LintedIdf, LintedLambda1}

object TestCase5 extends RtTestCase {
  val code_0 = ">>=(s => print(s))(input)\n"

  val tokens_1 = List(
    TokIdf(">>="),
    TokParenOpen,
    TokIdf("s"), TokEqGr, TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose,
    TokParenClose,
    TokParenOpen, TokIdf("input"), TokParenClose,
    TokEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("s"),
          ExprCall1(ExprIdf("print"), ExprIdf("s")),
        )
      ),
      ExprIdf("input")
    )

  override val mb_linted_3 = Some(LintedCall1(
    LintedCall1(
      LintedIdf(">>=", T_Func(T_Func(T_Str, T_RIO(T_Unit)), T_Func(T_RIO(T_Str), T_RIO(T_Unit)))),
      LintedLambda1(
        LintedIdf("s", T_Str),
        LintedCall1(
          LintedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
          LintedIdf("s", T_Str),
          T_RIO(T_Unit),
        ),
        T_Func(T_Str, T_RIO(T_Unit))
      ),
      T_Func(T_RIO(T_Str), T_RIO(T_Unit))
    ),
    LintedIdf("input", T_RIO(T_Str)),
    T_RIO(T_Unit),
  ))
}
