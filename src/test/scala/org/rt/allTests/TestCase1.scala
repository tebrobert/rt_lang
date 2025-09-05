package org.rt.allTests

import org.rt.RtTestCase
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.LintedIdf

object TestCase1 extends RtTestCase {
  val code_0 =
    """input"""

  val tokens_1 =
    List(
      TokIdf("input"),
    )

  val expr_2 =
    ExprIdf("input")

  val linted_3 =
    LintedIdf("input", T_RIO(T_Str))
}
