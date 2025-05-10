package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*

object TestCase1_2 extends RtTestCase {
  val code_0 = "(input)"
  val tokens_1 = List(TokenParenOpen, TokenIdf("input"), TokenParenClose)
  val expr_2 = ExprIdf("input")
}
