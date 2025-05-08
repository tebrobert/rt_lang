package utils

import lang.RtLib_2_Tokenize.{Token, TokenEq}
import lang.RtLib_3_Parse.Expr

object RtFail {
  def rtFail(msgs: String*) =
    throw new Exception (msgs.mkString)

  def fail_if(cond: Boolean, msg: String*) =
    if (cond)
        rtFail(msg:_*)

  def rt_assert(cond: Boolean, msg: String = "Assertion error.") =
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
    
  def rt_assert_type[EXPECTED <: (Token | Expr)](value: Token | Expr): EXPECTED =
    value match {
      case expected: EXPECTED => expected
      case _ => rtFail(s"Expected ..., got `$value`")
    }

  def rt_assert_type_TokenEq(value: Token | Expr): TokenEq.type =
    value match {
      case expected: TokenEq.type => expected
      case _ => rtFail(s"Expected ..., got `$value`")
    }
}
