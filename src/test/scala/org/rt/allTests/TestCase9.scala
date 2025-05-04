package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase9 extends RtTestCase {
  val code_0 = s"""print("Hey! What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Welcome, ...")\n"""
    + s"""print(name)\n"""

  val tokens_1 = List(
    TokenIdf("print"), TokenParenOpen,
    TokenLitStr("Hey! What is your name?"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("Welcome, ..."), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("name"), TokenParenClose, TokenEndl,
  )

  val expr_2 = lang.RtLib_3_Parse.ExprIdf("???")
}
