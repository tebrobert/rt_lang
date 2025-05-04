package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase14 extends RtTestCase {
  val code_0 = s"""greeting = "Hi!"\n"""
    + s"""print(greeting)\n"""
    + s"""print("What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Dear ".+(name).+(", welcome!"))\n"""

  val tokens_1 = List(
    TokenIdf("greeting"), TokenEq, TokenLitStr("Hi!"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("greeting"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("What is your name?"), TokenParenClose, TokenEndl,
    TokenIdf("name"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenLitStr("Dear "),
    TokenDot, TokenIdf("+"), TokenParenOpen, TokenIdf("name"), TokenParenClose,
    TokenDot, TokenIdf("+"), TokenParenOpen, TokenLitStr(", welcome!"), TokenParenClose, TokenParenClose, TokenEndl,
  )

  val expr_2 = lang.RtLib_3_Parse.ExprIdf("???")
}
