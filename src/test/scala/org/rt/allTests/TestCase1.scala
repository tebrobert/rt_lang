package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase1 extends RtTestCase {
  val code_0 = "input"
  val tokens_1 = List(TokenIdf("input"))
  val expr_2 = lang.RtLib_3_Parse.ExprIdf("input")
}
