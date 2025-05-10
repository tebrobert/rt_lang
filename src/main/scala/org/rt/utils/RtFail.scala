package org.rt.utils

import org.rt.lang.RtLib_2_Tokenize.{Token, TokenEq, TokenIdf, TokenLessMinus}
import org.rt.lang.RtLib_3_Parse.Expr

object RtFail {
  def rtFail(msgs: String*) =
    throw new Exception (msgs.mkString)

  def fail_if(cond: Boolean, msg: String*) =
    if (cond)
        rtFail(msg:_*)

  // todo - either
  def rt_assert(cond: Boolean, msg: String = "Assertion error."): Unit =
    fail_if(!cond, msg)

  def try_and_match[A, B](
      action: () => A,
      ifSuccess: A => B,
      ifFail: () => B,
  ): B =
    (try {
      Right(action())
    } catch {
      case _: Throwable => Left(ifFail())
    }).map(ifSuccess).merge
    
  def rt_assert_type_TokenEq(value: Token | Expr): TokenEq.type =
    value match {
      case expected: TokenEq.type => expected
      case _ => rtFail(s"Expected TokenEq, got `$value`")
    }

  def rt_assert_type_TokenIdf(value: Token | Expr): TokenIdf =
    value match {
      case expected: TokenIdf => expected
      case _ => rtFail(s"Expected TokenIdf, got `$value`")
    }

  def rt_assert_type_TokenLessMinus(value: Token | Expr): TokenLessMinus.type =
    value match {
      case expected: TokenLessMinus.type => expected
      case _ => rtFail(s"Expected TokenLessMinus, got `$value`")
    }
}
