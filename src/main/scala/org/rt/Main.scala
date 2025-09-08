package org.rt

import org.rt.lang.RtLib_2_Tokenize.Public.tokenize
import org.rt.lang.RtLib_5_Build.Public.{Built, BuiltRio, fullBuild}
import org.rt.utils.RtFail.rtFail
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    ZIO.succeed{
      println("HERE START")

      val b = fullBuild(
        """s <- input
          |print(s)
          |print(s)
          |""".stripMargin
      )
      org.rt.lang.RtLib_6_Run.run(b)

      println("HERE FINISH")
    }
  }