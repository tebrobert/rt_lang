package org.rt

import lang.RtLib_2_Tokenize.{TokenIdf, tokenize}
import org.rt.allTests.*
import zio.test.*

trait RtTestCase {
  val code_0: String
  val tokens_1: List[TokenIdf]
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
    )
}
