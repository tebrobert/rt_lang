package org.rt.utils

import org.rt.lang.RtLib_0_1_Types.{Typ, Typ1, Typ2, Unk0}
import org.rt.lang.RtLib_1_Tokenize.Public.{Tok, TokEq, TokIdf, TokLessMinus}
import org.rt.lang.RtLib_2_Parse.Expr
import org.rt.lang.RtLib_3_Lint.{Linted, LintedIdf}

object RtFail {
  case class RtFail(messages: Vector[String]) extends Throwable

  def rtFail(msgs: String*) =
    throw RtFail(msgs.toVector)

  def fail_if(cond: Boolean, msg: String*) =
    if (cond)
      rtFail(msg: _*)

  // todo - either
  def rt_assert(cond: Boolean, msg: String = "Assertion error."): Unit =
    fail_if(!cond, msg)

  inline
  def tryOrRecover[A](
    // todo - catch only specific errors (ideally, Either.Left's)
    inline action: () => A,
    inline recover: () => A,
  ): A =
    try {
      action()
    } catch {
      case _: RtFail => recover()
    }

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


  def rt_assert_type_TokenEq(value: Tok | Expr): TokEq.type =
    value match {
      case expected: TokEq.type => expected
      case _ => rtFail(s"Expected TokenEq, got `$value`")
    }

  def rt_assert_type_TokenIdf(value: Tok | Expr): TokIdf =
    value match {
      case expected: TokIdf => expected
      case _ => rtFail(s"Expected TokenIdf, got `$value`")
    }

  def rt_assert_type_TokenLessMinus(value: Tok | Expr): TokLessMinus.type =
    value match {
      case expected: TokLessMinus.type => expected
      case _ => rtFail(s"Expected TokenLessMinus, got `$value`")
    }

  def rt_assert_type_Typ1(value: Typ): Typ1 =
    value match {
      case expected: Typ1 => expected
      case _ => rtFail(s"Expected Typ1, got `$value`")
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

  def rt_assert_type_LintedIdf(value: Linted): LintedIdf =
    value match {
      case expected: LintedIdf => expected
      case _ => rtFail(s"Expected LintedIdf, got `$value`")
    }

  def rt_try[A](action: () => A): Either[RtFail, A] =
      try {
          Right(action())
      } catch {
        case e: RtFail =>
          Left(e)
        // traceback.format_exc()
        // RtError
      }

  def wip(msgs: String*): Nothing = {
    //caller_func_name = inspect.getouterframes (inspect.currentframe (), 2)[1][3]
    //print ("wip", caller_func_name)

    rtFail(msgs.prepended("The feature was not implemented. Work in progress..."): _*)
  }
}
