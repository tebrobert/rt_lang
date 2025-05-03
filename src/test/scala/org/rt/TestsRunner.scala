package org.rt

import lang.RtLib_2_Tokenize.{Token, tokenize}
import org.rt.allTests.*
import zio.test.*

trait RtTestCase {
  val code_0: String
  val tokens_1: List[Token]
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      allTestCases.map(testCase => test(testCase.getClass.getSimpleName) {
        assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
      })
      ++ allTestCases_22.map(testCase => test(testCase.getClass.getSimpleName) {
          assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
        })
    )
}
