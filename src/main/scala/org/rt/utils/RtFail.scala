package org.rt.utils

import org.rt.lang.RtLib_0_1_Types.{Typ, Typ1, Typ2, Unk0}
import org.rt.lang.RtLib_1_Tokenize.Public.{Tok, TokEq, TokIdf, TokLessMinus}
import org.rt.lang.RtLib_2_Parse.Expr
import org.rt.lang.RtLib_3_Lint.{Linted, LintedIdf}

object RtFail {
  case class RtFail(messages: Vector[String])
    extends Exception(messages.mkString(" "))

  object RtFail {
    def apply(msgs: String*): RtFail =
      RtFail(msgs.toVector)
  }

  def rtFailUnsafe(msgs: String*) =
    throw RtFail(msgs.toVector)

  def rtFail(msgs: String*) =
    Left(RtFail(msgs.toVector))

  def rtFailIfUnsafe(cond: Boolean, msg: String*) =
    if (cond)
      rtFailUnsafe(msg: _*)

  def rtFailIf(cond: Boolean, msg: String*) =
    if (cond)
      rtFail(msg: _*)
    else Right(())

  def rtAssertUnsafe(cond: Boolean, msg: String = "Assertion error."): Unit =
    rtFailIfUnsafe(!cond, msg)

  def rtAssert(cond: Boolean, msg: String = "Assertion error.") =
    rtFailIf(!cond, msg)

  inline
  def tryOrRecoverUnsafe[A](
    // todo - catch only specific errors (ideally, Either.Left's)
    inline action: () => A,
    inline recover: () => A,
  ): A =
    try {
      action()
    } catch {
      case _: RtFail => recover()
    }

  def rtAssertEqualUnsafe[ANY](
    actual: ANY,
    expected: ANY,
    label: String = "",
  ): ANY = {
    rtAssertUnsafe(
      expected.toString == actual.toString,
      s"Not equal `$label`: "
        + s"actual - `$actual`, expected - `$expected`.",
    )
    actual
  }


  def rtAssertTypeTokenEqUnsafe(value: Tok | Expr): TokEq.type =
    value match {
      case expected: TokEq.type => expected
      case _ => rtFailUnsafe(s"Expected TokenEq, got `$value`")
    }

  def rtAssertTypeTokenIdfUnsafe(value: Tok | Expr): TokIdf =
    value match {
      case expected: TokIdf => expected
      case _ => rtFailUnsafe(s"Expected TokenIdf, got `$value`")
    }

  def rtAssertTypeTokenLessMinusUnsafe(value: Tok | Expr): TokLessMinus.type =
    value match {
      case expected: TokLessMinus.type => expected
      case _ => rtFailUnsafe(s"Expected TokenLessMinus, got `$value`")
    }

  def rtAssertTypeTyp1Unsafe(value: Typ): Typ1 =
    value match {
      case expected: Typ1 => expected
      case _ => rtFailUnsafe(s"Expected Typ1, got `$value`")
    }

  def rt_assert_type_Typ2Unsafe(value: Typ): Typ2 =
    value match {
      case expected: Typ2 => expected
      case _ => rtFailUnsafe(s"Expected Typ2, got `$value`")
    }

  def rt_assert_type_Unk0_Unsafe(value: Typ): Unk0 =
    value match {
      case expected: Unk0 => expected
      case _ => rtFailUnsafe(s"Expected Unk0, got `$value`")
    }

  def rt_assert_type_LintedIdf_Unsafe(value: Linted): LintedIdf =
    value match {
      case expected: LintedIdf => expected
      case _ => rtFailUnsafe(s"Expected LintedIdf, got `$value`")
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

  def wipUnsafe(msgs: String*): Nothing = {
    //caller_func_name = inspect.getouterframes (inspect.currentframe (), 2)[1][3]
    //print ("wip", caller_func_name)

    rtFailUnsafe(msgs.prepended("The feature was not implemented. Work in progress..."): _*)
  }
}
