package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase14 extends RtTestCase {
  val code_0 = s"""greeting = "Hi!"\n"""
    + s"""print(greeting)\n"""
    + s"""print("What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Dear ".+(name).+(", welcome!"))\n"""
  
  val tokens_1 = List()
}
