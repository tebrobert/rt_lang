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
      test("1"){
        assertTrue(tokenize(TestCase1.code_0) == TestCase1.tokens_1)
      },
      test("2"){
        assertTrue(tokenize(TestCase2.code_0) == TestCase2.tokens_1)
      },
      test("3"){
        assertTrue(tokenize(TestCase3.code_0) == TestCase3.tokens_1)
      },
      test("4"){
        assertTrue(tokenize(TestCase4.code_0) == TestCase4.tokens_1)
      },
    )
}
