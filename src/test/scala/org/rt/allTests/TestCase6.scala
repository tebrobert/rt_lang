package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

object TestCase6 extends RtTestCase {
  val code_0 = ">>=(s => >>=(u => print(s))(print(s)))(input)\n"

  val tokens_1 = List(
    TokIdf(">>="),
    TokParenOpen, TokIdf("s"), TokEqGr,
    TokIdf(">>="),
    TokParenOpen, TokIdf("u"), TokEqGr, TokIdf("print"),
    TokParenOpen, TokIdf("s"), TokParenClose, TokParenClose,
    TokParenOpen, TokIdf("print"), TokParenOpen, TokIdf("s"), TokParenClose, TokParenClose,
    TokParenClose, TokParenOpen, TokIdf("input"), TokParenClose, TokEndl,
  )

  val expr_2 =
    ExprCall1(
      ExprCall1(
        ExprIdf(">>="),
        ExprLambda1(
          ExprIdf("s"),
          ExprCall1(
            ExprCall1(
              ExprIdf(">>="),
              ExprLambda1(
                ExprIdf("u"),
                ExprCall1(
                  ExprIdf("print"),
                  ExprIdf("s")
                )
              )
            ),
            ExprCall1(
              ExprIdf("print"),
              ExprIdf("s")
            )
          )
        )
      ),
      ExprIdf("input")
    )

  override val mb_typified_3 = Some(TypifiedCall1(
    TypifiedCall1(
      TypifiedIdf(">>=", T_Func(T_Func(T_Str, T_RIO(T_Unit)), T_Func(T_RIO(T_Str), T_RIO(T_Unit)))),
      TypifiedLambda1(
        TypifiedIdf("s", T_Str),
        TypifiedCall1(
          TypifiedCall1(
            TypifiedIdf(">>=", T_Func(T_Func(T_A0, T_RIO(T_Unit)), T_Func(T_RIO(T_Unit), T_RIO(T_Unit)))),
            TypifiedLambda1(
              TypifiedIdf("u", T_A0),
              TypifiedCall1(
                TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
                TypifiedIdf("s", T_Str),
                T_RIO(T_Unit)
              ),
              T_Func(T_A0, T_RIO(T_Unit)),
            ),
            T_Func(T_RIO(T_Unit), T_RIO(T_Unit)),
          ),
          TypifiedCall1(
            TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
            TypifiedIdf("s", T_Str),
            T_RIO(T_Unit)
          ),
          T_RIO(T_Unit)
        ),
        T_Func(T_Str, T_RIO(T_Unit)),
      ),
      T_Func(T_RIO(T_Str), T_RIO(T_Unit)),
    ),
    TypifiedIdf("input", T_RIO(T_Str)),
    T_RIO(T_Unit)
  ))
}
