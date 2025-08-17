package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.exprAndThen
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

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

  override val mb_typified_3 = Some(
    TypifiedCall1(
      TypifiedCall1(
        TypifiedIdf(">>=", T_Func(T_Func(T_Str, T_RIO(T_Unit)), T_Func(T_RIO(T_Str), T_RIO(T_Unit)))),
        TypifiedLambda1(
          TypifiedIdf("s", T_Str),
          TypifiedCall1(
            TypifiedCall1(
              TypifiedIdf(">>=", T_Func(T_Func(T_A0, T_RIO(T_Unit)), T_Func(T_RIO(T_Unit), T_RIO(T_Unit)))),
              TypifiedLambda1(
                TypifiedIdf("_", T_A0),
                TypifiedCall1(
                  TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
                  TypifiedIdf("s", T_Str),
                  T_RIO(T_Unit)
                ),
                T_Func(T_A0, T_RIO(T_Unit))
              ),
              T_Func(T_RIO(T_Unit), T_RIO(T_Unit))
            ),
            TypifiedCall1(
              TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
              TypifiedIdf("s", T_Str),
              T_RIO(T_Unit)
            ),
            T_RIO(T_Unit)
          ),
          T_Func(T_Str, T_RIO(T_Unit))
        ),
        T_Func(T_RIO(T_Str), T_RIO(T_Unit))
      ),
      TypifiedIdf("input", T_RIO(T_Str)),
      T_RIO(T_Unit)
    )
  )
}
