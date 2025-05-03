package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase2 extends RtTestCase {
  val code_0 = "(s => s)(input)\n"
  val tokens_1 = List(
    TokenParenOpen, TokenIdf("s"), TokenEqGr, TokenIdf("s"), TokenParenClose,
    TokenParenOpen, TokenIdf("input"), TokenParenClose,
    TokenEndl,
  )
}
