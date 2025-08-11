package org.rt.utils

import org.rt.lang.RtLib_0_1_Types.{Typ, Typ2, Unk0}
import org.rt.lang.RtLib_2_Tokenize.{Token, TokenEq, TokenIdf, TokenLessMinus}
import org.rt.lang.RtLib_3_Parse.Expr

object RtFail {
  def rtFail(msgs: String*) =
    throw new Exception(msgs.mkString)

  def fail_if(cond: Boolean, msg: String*) =
    if (cond)
      rtFail(msg: _*)

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

  def rt_assert_equal[ANY](
    actual: ANY,
    expected: ANY,
    label: String = "",
  ): ANY = {
    rt_assert(
      expected.toString == actual.toString,
      s"Not equal `$label`: "
        + s"actual - `$actual`, expected - `$expected`.",
    )
    actual
  }


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

  def rt_assert_type_Typ2(value: Typ): Typ2 =
    value match {
      case expected: Typ2 => expected
      case _ => rtFail(s"Expected Typ2, got `$value`")
    }

  def rt_assert_type_Unk0(value: Typ): Unk0 =
    value match {
      case expected: Unk0 => expected
      case _ => rtFail(s"Expected Unk0, got `$value`")
    }

  def wip(msgs: String*): Nothing = {
    //caller_func_name = inspect.getouterframes (inspect.currentframe (), 2)[1][3]
    //print ("wip", caller_func_name)

    rtFail(msgs.prepended("The feature was not implemented. Work in progress..."): _*)
  }
}
