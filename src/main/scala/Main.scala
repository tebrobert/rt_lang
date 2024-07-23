import lang.RtLib_2_Tokenize.tokenize
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  val code ="""
      |greeting = "Hi!"
      |print(greeting)
      |print("What is your name?")
      |name <- input
      |print("Welcome, " + name + "!")
      |
      |f = x => 1 + x * 10
      |print("The result is " + str(f(5)))
      |""".stripMargin

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    val x = tokenize("input")
    printLine(x.toString)
  }