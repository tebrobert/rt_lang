package org.rt.allTests

import org.rt.RtTestCase
import org.rt.RuntimeMock.{InputMock, PrintMock}
import org.rt.TestHelpers.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.lang.RtLib_2_Parse.*
import org.rt.lang.RtLib_3_Lint.*

object TestCase17 extends RtTestCase {
  val code_0 =
    """a"""

  val tokens_1 =
    List(
      TokIdf("a"),
    )

  val expr_2 =
    ExprIdf("a")

  val linted_3 = // todo likely should fail here
    LintedIdf("a", T_A0)
}
