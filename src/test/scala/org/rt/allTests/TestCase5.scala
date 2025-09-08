package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RunMock.{InputMock, PrintMock, RunMocks}
import org.rt.lang.RtLib_0_2_Builtins.{T_RIO, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{LintedCall1, LintedIdf, LintedLambda1}

object TestCase5 extends RtTestCase {
  val code_0 = ">>=(s => print(s))(input)\n"

  val tokens_1 = List(
    TokIdf(">>="),
    TokParenOpen,
    TokIdf("s"), TokEqGr, TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose,
    TokParenClose,
    TokParenOpen, TokIdf("input"), TokParenClose,
    TokEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("s"),
          ExprCall1(ExprIdf("print"), ExprIdf("s")),
        )
      ),
      ExprIdf("input")
    )

  val linted_3 =
    LintedCall1(
      LintedCall1(
        LintedIdf(">>=", (T_Str tTo T_RIO(T_Unit)) tTo (T_RIO(T_Str) tTo T_RIO(T_Unit))),
        LintedLambda1(
          LintedIdf("s", T_Str),
          LintedCall1(
            LintedIdf("print", T_Str tTo T_RIO(T_Unit)),
            LintedIdf("s", T_Str),
            T_RIO(T_Unit),
          ),
          T_Str tTo T_RIO(T_Unit),
        ),
        T_RIO(T_Str) tTo T_RIO(T_Unit),
      ),
      LintedIdf("input", T_RIO(T_Str)),
      T_RIO(T_Unit),
    )

  override val mb_mock_4 =
    List(
      RunMocks(List(InputMock("s"), PrintMock("s"))),
    )
}
