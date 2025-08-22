package org.rt

import org.rt.lang.RtLib_2_Tokenize.Public.{Tok, tokenize}
import org.rt.lang.RtLib_3_Parse.{Expr, parse}
import org.rt.lang.RtLib_4_Lint.{Linted, lint}

import zio.test.*

trait RtTestCase {
  final val name = this.getClass.getSimpleName

  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val mb_linted_3: Option[Linted] = None
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      allTestCases.map(testCase => test("tokenize " + testCase.name) {
        assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
      })
        ++ allTestCases.map(testCase => test("parse " + testCase.name) {
        assertTrue(parse(testCase.tokens_1) == testCase.expr_2)
      })
        ++ allTestCases.flatMap(testCase =>
        testCase.mb_linted_3.map { linted_3 =>
          test("lint " + testCase.name) {
            assertTrue(lint(testCase.expr_2) == linted_3)
          }
        }
      )
    )
}