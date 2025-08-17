package org.rt

import lang.RtLib_2_Tokenize.tokenize
import lang.RtLib_3_Parse.{Expr, parse}
import org.rt.allTests.*
import org.rt.lang.RtLib_2_Tokenize.Classes.Tok
import org.rt.lang.RtLib_4_Lint.{Linted, lint}
import zio.test.*

trait RtTestCase {
  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val mb_linted_3: Option[Linted] = None
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      allTestCases.map(testCase => test("tokenize "+testCase.getClass.getSimpleName) {
        assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
      })
      ++ allTestCases.map(testCase => test("parse "+testCase.getClass.getSimpleName) {
          assertTrue(parse(testCase.tokens_1) == testCase.expr_2)
        })
      ++ allTestCases.flatMap(testCase =>
        testCase.mb_linted_3.map { linted_3 =>
          test("lint " + testCase.getClass.getSimpleName) {
            assertTrue(lint(testCase.expr_2) == linted_3)
          }
        }
      )
    )
}
