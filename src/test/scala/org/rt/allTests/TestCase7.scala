package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase7 extends RtTestCase {
  val code_0 = "x <- input"
    + "\ny <- input"
    + "\nprint(x)"
    + "\nprint(y)"

  val tokens_1 = List(
    TokenIdf(s = "x"), TokenLessMinus, TokenIdf(s = "input"), TokenEndl,
    TokenIdf(s = "y"), TokenLessMinus, TokenIdf(s = "input"), TokenEndl,
    TokenIdf(s = "print"), TokenParenOpen, TokenIdf(s = "x"), TokenParenClose, TokenEndl,
    TokenIdf(s = "print"), TokenParenOpen, TokenIdf(s = "y"), TokenParenClose,
  )
}
