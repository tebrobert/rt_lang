package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_Func, T_RIO, T_Str}
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

object TestCase2 extends RtTestCase {
  val code_0 = "(s => s)(input)\n"

  val tokens_1 = List(
    TokenParenOpen, TokenIdf("s"), TokenEqGr, TokenIdf("s"), TokenParenClose,
    TokenParenOpen, TokenIdf("input"), TokenParenClose,
    TokenEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprLambda1(ExprIdf("s"), ExprIdf("s")),
      ExprIdf( "input"),
    )

  override val mb_typified_3 = Some(TypifiedCall1(
    TypifiedLambda1.create(
      TypifiedIdf("s", T_RIO(T_Str)),
      TypifiedIdf("s", T_RIO(T_Str)),
      T_Func(T_RIO(T_Str), T_RIO(T_Str))
    ),
    TypifiedIdf("input", T_RIO(T_Str)),
    T_RIO(T_Str),
  ))
}
