package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

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

  override val mb_typified_3 = Some(TypifiedCall1(
    TypifiedCall1(
      TypifiedLambda1(
        TypifiedIdf("s", T_Func(T_A0, T_A0)),
        TypifiedIdf("s", T_Func(T_RIO(T_Str), T_RIO(T_Str))),
        T_Func(T_Func(T_A0, T_A0), T_Func(T_RIO(T_Str), T_RIO(T_Str))),
      ),
      TypifiedLambda1(
        TypifiedIdf("s", T_A0),
        TypifiedIdf("s", T_A0),
        T_Func(T_A0, T_A0),
      ),
      T_Func(T_RIO(T_Str), T_RIO(T_Str)),
    ),
    TypifiedIdf("input", T_RIO(T_Str)),
    T_RIO(T_Str),
  ))
}
