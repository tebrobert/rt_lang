package org.rt

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*
import org.rt.utils.RtFail.{rt_assert_type_Typ1, rt_assert_type_Typ2}
import org.rt.utils.RtList.rtMatch

import scala.annotation.tailrec

object TestHelpers {
  val T_RIO_Unit = T_RIO(T_Unit)
  val T_RIO_Str = T_RIO(T_Str)
  val T_Str_To_RIO_Unit = T_Str tTo T_RIO_Unit
  val T_Str_To_RIO_Str = T_Str tTo T_RIO_Str

  val vPrint = builtin_print
  val vInput = builtin_input
  val vPure = builtin_pure

  @tailrec
  def exprCurrCall(
    f: Expr,
    xs: Expr*,
  ): Expr =
    xs.toList.rtMatch(
      caseEmpty = () => f,
      caseAtLeast1 = (headX, tailX) =>
        exprCurrCall(ExprCall1(f, headX), tailX:_*),
    )

  @tailrec
  def lintedCurrCall(
    f: Linted,
    xs: Linted*,
  ): Linted =
    xs.toList.rtMatch(
      caseEmpty = () => f,
      caseAtLeast1 = (headX, tailX) => {
        val typ2F = rt_assert_type_Typ2(f.typ)
        lintedCurrCall(LintedCall1(f, headX, typ2F.t2), tailX: _*)
      },
    )

  def exprAndThen(
    resName: String,
    expr: Expr,
    exprNext: Expr,
  ) =
    exprCurrCall(
      ExprIdf(builtin_flatmap),
      ExprLambda1(ExprIdf(resName), exprNext),
      expr,
    )

  def exprEqAndThen(
    resName: String,
    expr: Expr,
    exprNext: Expr,
  ) =
    exprAndThen(
      resName,
      ExprCall1(ExprIdf(builtin_pure), expr),
      exprNext,
    )

  def lintedAndThen(
    resName: String,
    linted: Linted,
    lintedNext: Linted,
  ) = {
    val resTyp = rt_assert_type_Typ1(linted.typ).t1 // todo - try better typing

    lintedCurrCall(
      LintedIdf(
        builtin_flatmap,
        (resTyp tTo lintedNext.typ) tTo (T_RIO(resTyp) tTo lintedNext.typ),
      ),
      LintedLambda1(
        LintedIdf(resName, resTyp),
        lintedNext,
        resTyp tTo lintedNext.typ,
      ),
      linted,
    )
  }

  def lintedEqAndThen(
    resName: String,
    linted: Linted,
    lintedNext: Linted,
  ) =
    lintedAndThen(
      resName,
      LintedCall1(
        LintedIdf(builtin_pure, linted.typ tTo T_RIO(linted.typ)),
        linted,
        T_RIO(linted.typ),
      ),
      lintedNext,
    )
}
