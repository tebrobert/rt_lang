package org.rt

import org.rt.lang.RtLib_2_Tokenize.Public.tokenize
import org.rt.lang.RtLib_5_Build.Public.{Built, BuiltBrick, fullBuildScala}
import org.rt.utils.RtFail.rtFail
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    ZIO.succeed{
      def run(
        built: Built,
      ): Unit =
        built match {
          case BuiltBrick(run) => run()
          case _ => rtFail("runtime")
        }

      println("HERE START")

      val b = fullBuildScala(
        """s <- input
          |print(s)
          |print(s)
          |""".stripMargin
      )
      run(b)

      println("HERE FINISH")
      ()
    }
  }