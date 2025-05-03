package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase12 extends RtTestCase {
  val code_0 = s"""greeting = "Hey! What is your name?"\n"""
    + s"""print(greeting)\n"""
    + s"""name <- input\n"""
    + s"""print(+(name)("Welcome, "))"""

  val tokens_1 = List()
}
