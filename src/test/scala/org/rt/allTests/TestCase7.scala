package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Func, T_RIO, T_Str, T_Unit}
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.{TypifiedCall1, TypifiedIdf, TypifiedLambda1}

object TestCase7 extends RtTestCase {
  val code_0 = ""
    + "x <- input\n"
    + "y <- input\n"
    + "print(x)\n"
    + "print(y)"

  val tokens_1 = List(
    TokenIdf("x"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("y"), TokenLessMinus, TokenIdf("input"), TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("x"), TokenParenClose, TokenEndl,
    TokenIdf("print"), TokenParenOpen, TokenIdf("y"), TokenParenClose,
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

  override val mb_typified_3 = Some(
    TypifiedCall1(
      TypifiedCall1(
        TypifiedIdf(">>=", T_Func(T_Func(T_Str, T_RIO(T_Unit)), T_Func(T_RIO(T_Str), T_RIO(T_Unit)))),
        TypifiedLambda1(
          TypifiedIdf("x", T_Str),
          TypifiedCall1(
            TypifiedCall1(
              TypifiedIdf(">>=", T_Func(T_Func(T_Str, T_RIO(T_Unit)), T_Func(T_RIO(T_Str), T_RIO(T_Unit)))),
              TypifiedLambda1(
                TypifiedIdf("y", T_Str),
                TypifiedCall1(
                  TypifiedCall1(
                    TypifiedIdf(">>=", T_Func(T_Func(T_A0, T_RIO(T_Unit)), T_Func(T_RIO(T_Unit), T_RIO(T_Unit)))),
                    TypifiedLambda1(
                      TypifiedIdf("_", T_A0),
                      TypifiedCall1(
                        TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
                        TypifiedIdf("y", T_Str),
                        T_RIO(T_Unit)
                      ),
                      T_Func(T_A0, T_RIO(T_Unit)),
                    ),
                    T_Func(T_RIO(T_Unit), T_RIO(T_Unit)),
                  ),
                  TypifiedCall1(
                    TypifiedIdf("print", T_Func(T_Str, T_RIO(T_Unit))),
                    TypifiedIdf("x", T_Str),
                    T_RIO(T_Unit),
                  ),
                  T_RIO(T_Unit),
                ),
                T_Func(T_Str, T_RIO(T_Unit)),
              ),
              T_Func(T_RIO(T_Str), T_RIO(T_Unit)),
            ),
            TypifiedIdf("input", T_RIO(T_Str)),
            T_RIO(T_Unit)
          ),
          T_Func(T_Str, T_RIO(T_Unit)),
        ),
        T_Func(T_RIO(T_Str), T_RIO(T_Unit)),
      ),
      TypifiedIdf("input", T_RIO(T_Str)),
      T_RIO(T_Unit),
    )
  )
}
