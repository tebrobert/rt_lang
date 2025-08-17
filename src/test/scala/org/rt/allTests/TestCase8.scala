package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_1_Types.Typ
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{Typified, TypifiedCall1, TypifiedIdf, TypifiedLambda1}

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

  def typifiedAndThen(
    resultTuple: (String, Typ),
    typified: Typified,
    typifiedNext: Typified,
  ) = {
    val typifiedResult = TypifiedIdf.apply.tupled(resultTuple)

    TypifiedCall1(
      TypifiedCall1(
        TypifiedIdf(
          ">>=",
          (typifiedResult.typ tTo T_RIO_Unit)
            tTo (T_RIO_Unit /*T_RIO(typifiedResult.typ)*/ tTo typifiedNext.typ)
        ),
        TypifiedLambda1(typifiedResult, typifiedNext, typifiedResult.typ tTo T_RIO_Unit),
        T_RIO_Unit /*T_RIO(typifiedResult.typ)*/ tTo typifiedNext.typ,
      ),
      typified,
      typifiedNext.typ,
    )
  }

  override val mb_typified_3 =
    Some({
      (("s", T_Str), TypifiedIdf("input", T_RIO_Str),
        (("_", T_A0), TypifiedCall1(TypifiedIdf("print", T_Str_To_RIO_Unit), TypifiedIdf("s", T_Str), T_RIO_Unit),
          TypifiedCall1(TypifiedIdf("print", T_Str_To_RIO_Unit), TypifiedIdf("s", T_Str), T_RIO_Unit),
        )
      )

      TypifiedCall1(
        TypifiedCall1(
          TypifiedIdf(">>=", (T_Str tTo T_RIO_Unit) tTo (T_RIO_Str tTo T_RIO_Unit)),
          TypifiedLambda1(
            TypifiedIdf("s", T_Str),
            TypifiedCall1(
              TypifiedCall1(
                TypifiedIdf(">>=", (T_A0 tTo T_RIO_Unit) tTo (T_RIO_Unit tTo T_RIO_Unit)),
                TypifiedLambda1(
                  TypifiedIdf("_", T_A0),
                  TypifiedCall1(
                    TypifiedIdf("print", T_Str tTo T_RIO_Unit),
                    TypifiedIdf("s", T_Str),
                    T_RIO_Unit,
                  ),
                  T_A0 tTo T_RIO_Unit,
                ),
                T_RIO_Unit tTo T_RIO_Unit,
              ),
              TypifiedCall1(
                TypifiedIdf("print", T_Str tTo T_RIO_Unit),
                TypifiedIdf("s", T_Str),
                T_RIO_Unit,
              ),
              T_RIO_Unit,
            ),
            T_Str tTo T_RIO_Unit,
          ),
          T_RIO_Str tTo T_RIO_Unit,
        ),
        TypifiedIdf("input", T_RIO_Str),
        T_RIO_Unit,
      )
    })
}
