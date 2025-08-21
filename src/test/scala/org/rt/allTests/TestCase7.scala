package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_RIO, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.{LintedCall1, LintedIdf, LintedLambda1}

object TestCase7 extends RtTestCase {
  val code_0 = ""
    + "x <- input\n"
    + "y <- input\n"
    + "print(x)\n"
    + "print(y)"

  val tokens_1 = List(
    TokIdf("x"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("y"), TokLessMinus, TokIdf("input"), TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("x"), TokParenClose, TokEndl,
    TokIdf("print"), TokParenOpen, TokIdf("y"), TokParenClose,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("x"),
          ExprCall1(
            ExprCall1(
              ExprIdf(">>="),
              ExprLambda1(
                ExprIdf("y"),
                ExprCall1(
                  ExprCall1(
                    ExprIdf(">>="),
                    ExprLambda1(
                      ExprIdf("_"),
                      ExprCall1(
                        ExprIdf("print"),
                        ExprIdf("y")
                      )
                    )
                  ),
                  ExprCall1(
                    ExprIdf("print"),
                    ExprIdf("x")
                  )
                )
              )
            ),
            ExprIdf("input")
          )
        )
      ),
      ExprIdf("input")
    )

  override val mb_linted_3 = Some(
    LintedCall1(
      LintedCall1(
        LintedIdf(">>=", (T_Str tTo T_RIO(T_Unit)) tTo (T_RIO(T_Str) tTo T_RIO(T_Unit))),
        LintedLambda1(
          LintedIdf("x", T_Str),
          LintedCall1(
            LintedCall1(
              LintedIdf(">>=", (T_Str tTo T_RIO(T_Unit)) tTo (T_RIO(T_Str) tTo T_RIO(T_Unit))),
              LintedLambda1(
                LintedIdf("y", T_Str),
                LintedCall1(
                  LintedCall1(
                    LintedIdf(">>=", (T_A0 tTo T_RIO(T_Unit)) tTo (T_RIO(T_Unit) tTo T_RIO(T_Unit))),
                    LintedLambda1(
                      LintedIdf("_", T_A0),
                      LintedCall1(
                        LintedIdf("print", T_Str tTo T_RIO(T_Unit)),
                        LintedIdf("y", T_Str),
                        T_RIO(T_Unit)
                      ),
                      T_A0 tTo T_RIO(T_Unit),
                    ),
                    T_RIO(T_Unit) tTo T_RIO(T_Unit),
                  ),
                  LintedCall1(
                    LintedIdf("print", T_Str tTo T_RIO(T_Unit)),
                    LintedIdf("x", T_Str),
                    T_RIO(T_Unit),
                  ),
                  T_RIO(T_Unit),
                ),
                T_Str tTo T_RIO(T_Unit),
              ),
              T_RIO(T_Str) tTo T_RIO(T_Unit),
            ),
            LintedIdf("input", T_RIO(T_Str)),
            T_RIO(T_Unit)
          ),
          T_Str tTo T_RIO(T_Unit),
        ),
        T_RIO(T_Str) tTo T_RIO(T_Unit),
      ),
      LintedIdf("input", T_RIO(T_Str)),
      T_RIO(T_Unit),
    )
  )
}
