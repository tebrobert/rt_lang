package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase8 extends RtTestCase {
  val code_0 = "s <- input\n"
    + "print(s)\n"
    + "print(s)\n"

  val tokens_1 = List(
    TokenIdf("s"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenEndl,
  )
}
