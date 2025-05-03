package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase7 extends RtTestCase {
  val code_0 = "x <- input"
    + "\ny <- input"
    + "\nprint(x)"
    + "\nprint(y)"

  val tokens_1 = List(
    TokenIdf(">>="), TokenParenOpen, TokenIdf("x"), TokenEqGr,
    TokenIdf(">>="), TokenParenOpen, TokenIdf("y"), TokenEqGr,
    TokenIdf(">>="), TokenParenOpen,
    TokenIdf("_"), TokenEqGr, TokenIdf("print"), TokenParenOpen, TokenIdf("y"), TokenParenClose, TokenParenClose,
    TokenParenOpen, TokenIdf("print"), TokenParenOpen, TokenIdf("x"), TokenParenClose, TokenParenClose,
    TokenParenClose, TokenParenOpen, TokenIdf("input"), TokenParenClose, TokenParenClose, TokenParenOpen,
    TokenIdf("input"), TokenParenClose,
  )
}
