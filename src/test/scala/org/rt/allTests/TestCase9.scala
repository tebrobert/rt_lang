package org.rt.allTests

import lang.RtLib_2_Tokenize.*
import org.rt.RtTestCase

object TestCase9 extends RtTestCase {
  val code_0 = s"""print("Hey! What is your name?")\n"""
    + s"""name <- input\n"""
    + s"""print("Welcome, ...")\n"""
    + s"""print(name)\n"""

  val tokens_1 = List()
}
