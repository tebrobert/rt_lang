package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_1_Types.Typ
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1}

object TestCase8 extends RtTestCase {
  val code_0 =
    """s <- input
      |print(s)
      |print(s)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("s"), TokLessMinus, TokIdf("input"), TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose, TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose, TokEndl,
    )

  val expr_2 =
    exprAndThen("s", ExprIdf("input"),
      exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("s")),
        ExprCall1(ExprIdf("print"), ExprIdf("s")),
      ),
    )

  def lintedAndThen(
    resultTuple: (String, Typ),
    linted: Linted,
    lintedNext: Linted,
  ) = {
    val typifiedResult = LintedIdf.apply.tupled(resultTuple)

    LintedCall1(
      LintedCall1(
        LintedIdf(
          ">>=",
          (typifiedResult.typ tTo T_RIO_Unit)
            tTo (T_RIO_Unit /*T_RIO(typifiedResult.typ)*/ tTo lintedNext.typ)
        ),
        LintedLambda1(typifiedResult, lintedNext, typifiedResult.typ tTo T_RIO_Unit),
        T_RIO_Unit /*T_RIO(typifiedResult.typ)*/ tTo lintedNext.typ,
      ),
      linted,
      lintedNext.typ,
    )
  }

  override val mb_linted_3 =
    Some({
      (("s", T_Str), LintedIdf("input", T_RIO_Str),
        (("_", T_A0), LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("s", T_Str), T_RIO_Unit),
          LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("s", T_Str), T_RIO_Unit),
        )
      )

      LintedCall1(
        LintedCall1(
          LintedIdf(">>=", (T_Str tTo T_RIO_Unit) tTo (T_RIO_Str tTo T_RIO_Unit)),
          LintedLambda1(
            LintedIdf("s", T_Str),
            LintedCall1(
              LintedCall1(
                LintedIdf(">>=", (T_A0 tTo T_RIO_Unit) tTo (T_RIO_Unit tTo T_RIO_Unit)),
                LintedLambda1(
                  LintedIdf("_", T_A0),
                  LintedCall1(
                    LintedIdf("print", T_Str tTo T_RIO_Unit),
                    LintedIdf("s", T_Str),
                    T_RIO_Unit,
                  ),
                  T_A0 tTo T_RIO_Unit,
                ),
                T_RIO_Unit tTo T_RIO_Unit,
              ),
              LintedCall1(
                LintedIdf("print", T_Str tTo T_RIO_Unit),
                LintedIdf("s", T_Str),
                T_RIO_Unit,
              ),
              T_RIO_Unit,
            ),
            T_Str tTo T_RIO_Unit,
          ),
          T_RIO_Str tTo T_RIO_Unit,
        ),
        LintedIdf("input", T_RIO_Str),
        T_RIO_Unit,
      )
    })
}
