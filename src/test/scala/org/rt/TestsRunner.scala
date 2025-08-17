package org.rt

import lang.RtLib_2_Tokenize.tokenize
import lang.RtLib_3_Parse.{Expr, parse}
import org.rt.allTests.*
import org.rt.lang.RtLib_2_Tokenize.Classes.Tok
import org.rt.lang.RtLib_4_Typify.{Typified, typify}
import zio.test.*

trait RtTestCase {
  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val mb_typified_3: Option[Typified] = None
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
        testCase.mb_typified_3.map { typified_3 =>
          test("typify " + testCase.getClass.getSimpleName) {
            assertTrue(typify(testCase.expr_2) == typified_3)
          }
        }
      )
    )
}
