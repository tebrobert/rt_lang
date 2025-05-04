package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase6 extends RtTestCase {
  val code_0 = ">>=(s => >>=(u => print(s))(print(s)))(input)\n"

  val tokens_1 = List(
    TokenIdf(">>="),
    TokenParenOpen, TokenIdf("s"), TokenEqGr,
    TokenIdf(">>="),
    TokenParenOpen, TokenIdf("u"), TokenEqGr, TokenIdf("print"),
    TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenParenClose,
    TokenParenOpen, TokenIdf("print"), TokenParenOpen, TokenIdf("s"), TokenParenClose, TokenParenClose,
    TokenParenClose, TokenParenOpen, TokenIdf("input"), TokenParenClose, TokenEndl,
  )

  val expr_2 = lang.RtLib_3_Parse.ExprIdf("???")
}
