package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*

object TestCase13 extends RtTestCase {
  val code_0 =
    s"""greeting = "Hey! What is your name?"
       |$vPrint(greeting)
       |name <- $vInput
       |result = +(name)("Welcome, ")
       |$vPrint(result)""".stripMargin

  val tokens_1 =
    List(
      TokIdf("greeting"), TokEq, TokLitStr("Hey! What is your name?"), TokEndl,
      TokIdf(vPrint), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
      TokIdf("name"), TokLessMinus, TokIdf(vInput), TokEndl,

      TokIdf("result"), TokEq, TokIdf("+"),
      TokParenOpen, TokIdf("name"), TokParenClose,
      TokParenOpen, TokLitStr("Welcome, "), TokParenClose, TokEndl,

      TokIdf(vPrint), TokParenOpen, TokIdf("result"), TokParenClose,
    )

  val expr_2 =
    exprEqAndThen("greeting", ExprLitStr("Hey! What is your name?"),
      exprAndThen("_", ExprCall1(ExprIdf(vPrint), ExprIdf("greeting")),
        exprAndThen("name", ExprIdf(vInput),
          exprEqAndThen("result", exprCalls(ExprIdf(vPlus), ExprIdf("name"), ExprLitStr("Welcome, ")),
            ExprCall1(ExprIdf(vPrint), ExprIdf("result")),
          ),
        ),
      ),
    )

  val linted_3 =
    lintedEqAndThen("greeting", LintedLit("Hey! What is your name?", T_Str),
      lintedAndThen("_", LintedCall1(LintedIdf(vPrint, T_Str_To_RIO_Unit), LintedIdf("greeting", T_Str), T_RIO_Unit),
        lintedAndThen("name", LintedIdf(vInput, T_RIO_Str),
          lintedEqAndThen("result", lintedCalls(LintedIdf(vPlus, T_Str_To_Str_To_Str), LintedIdf("name", T_Str), LintedLit("Welcome, ", T_Str)),
            LintedCall1(LintedIdf(vPrint, T_Str_To_RIO_Unit), LintedIdf("result", T_Str), T_RIO_Unit),
          ),
        ),
      ),
    )
}
