package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase13 extends RtTestCase {
  val code_0 = s"""greeting = "Hey! What is your name?"\n"""
    + s"""print(greeting)\n"""
    + s"""name <- input\n"""
    + s"""result = +(name)("Welcome, ")\n"""
    + s"""print(result)"""

  val tokens_1 = List(
    TokenIdf("greeting"), TokenEq, TokenLitStr("Hey! What is your name?"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("greeting"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("result"), TokenEq, TokenIdf("+"),
    TokenParenOpen, TokenIdf("name"), TokenParenClose,
    TokenParenOpen, TokenLitStr("Welcome, "), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("result"), TokenParenClose,
  )

  val expr_2 = lang.RtLib_3_Parse.ExprIdf("???")
}
