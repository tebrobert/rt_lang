package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_RIO, T_Str}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.TypifiedIdf

object TestCase1_2 extends RtTestCase {
  val code_0 = "(input)"
  val tokens_1 = List(TokParenOpen, TokIdf("input"), TokParenClose)
  val expr_2 = ExprIdf("input")
  override val mb_typified_3 = Some(TypifiedIdf("input", T_RIO(T_Str)))
}
