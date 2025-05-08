package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import lang.RtLib_3_Parse.ExprIdf
import org.rt.RtTestCase

object TestCase1_2 extends RtTestCase {
  val code_0 = "(input)"
  val tokens_1 = List(TokenParenOpen, TokenIdf("input"), TokenParenClose)
  val expr_2 = ExprIdf("input")
}
