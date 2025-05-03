package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase5 extends RtTestCase {
  val code_0 = ">>=(s => print(s))(input)\n"
  val tokens_1 = List(
    TokenIdf(">>="),
    TokenParenOpen,
    TokenIdf("s"), TokenEqGr, TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose,
    TokenParenClose,
    TokenParenOpen, TokenIdf("input"), TokenParenClose,
    TokenEndl,
  )
}
