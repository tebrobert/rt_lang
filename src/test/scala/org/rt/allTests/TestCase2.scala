package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.T_Func
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

object TestCase2 extends RtTestCase {
  val code_0 =
    """(s => s)(input)
      |""".stripMargin

  val tokens_1 =
    List(
      TokParenOpen, TokIdf("s"), TokEqGr, TokIdf("s"), TokParenClose,
      TokParenOpen, TokIdf("input"), TokParenClose,
      TokEndl,
    )

  val expr_2 =
    ExprCall1(
      ExprLambda1(ExprIdf("s"), ExprIdf("s")),
      ExprIdf("input"),
    )

  override val mb_typified_3 =
    Some(
      TypifiedCall1(
        TypifiedLambda1(
          TypifiedIdf("s", T_RIO_Str), TypifiedIdf("s", T_RIO_Str),
          T_RIO_Str tTo T_RIO_Str,
        ),
        TypifiedIdf("input", T_RIO_Str),
        T_RIO_Str,
      ))
}
