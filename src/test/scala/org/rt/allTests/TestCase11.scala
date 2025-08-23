package org.rt.allTests

import org.rt.RtTestCase
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*

object TestCase11 extends RtTestCase {
  val code_0 =
    """greeting = "Hey! What is your name?"
      |print(greeting)
      |name <- input
      |print("Welcome, ...")
      |print(name)
      |""".stripMargin

  val tokens_1 =
    List(
      TokIdf("greeting"), TokEq, TokLitStr("Hey! What is your name?"), TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose, TokEndl,
      TokIdf("name"), TokLessMinus, TokIdf("input"), TokEndl,
      TokIdf("print"), TokParenOpen, TokLitStr("Welcome, ..."), TokParenClose, TokEndl,
      TokIdf("print"), TokParenOpen, TokIdf("name"), TokParenClose, TokEndl,
    )

  val expr_2 =
    exprAndThen("greeting", ExprCall1(ExprIdf("pure"), ExprLitStr("Hey! What is your name?")),
      exprAndThen("_", ExprCall1(ExprIdf("print"), ExprIdf("greeting")),
        exprAndThen("name", ExprIdf("input"),
          exprAndThen("_", ExprCall1(ExprIdf("print"), ExprLitStr("Welcome, ...")),
            ExprCall1(ExprIdf("print"), ExprIdf("name")),
          ),
        ),
      ),
    )

  override val mb_linted_3 =
    Some(
      lintedAndThen(("greeting", T_Str), LintedCall1(LintedIdf("pure", T_Str_To_RIO_Str), LintedLit("Hey! What is your name?", T_Str), T_RIO_Str),
        lintedAndThen(("_", T_Unit), LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("greeting", T_Str), T_RIO_Unit),
          lintedAndThen(("name", T_Str), LintedIdf("input", T_RIO_Str),
            lintedAndThen(("_", T_Unit), LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedLit("Welcome, ...", T_Str), T_RIO_Unit),
              LintedCall1(LintedIdf("print", T_Str_To_RIO_Unit), LintedIdf("name", T_Str), T_RIO_Unit),
            ),
          ),
        ),
      )
    )
}
