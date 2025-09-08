package org.rt

import org.rt.lang.RtLib_2_Tokenize.Public.tokenize
import org.rt.lang.RtLib_5_Build.Public
import org.rt.lang.RtLib_5_Build.Public.{Brick, BrickInput, BrickPrint, Built, BuiltRio, BuiltStr, BuiltUnit, fullBuild}
import org.rt.utils.RtFail.rtFail
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    ZIO.succeed{
      println("HERE START")

      def brickRunner(brick: Brick): BuiltRio =
          brick match {
            case BrickInput => BuiltRio(() => BuiltStr(scala.io.StdIn.readLine()))
            case BrickPrint(s) => BuiltRio(() => BuiltUnit(println(s)))
          }

      val code =
        """s <- input
          |print(s)
          |print(s)
          |""".stripMargin

      val b = fullBuild(code, brickRunner)
      org.rt.lang.RtLib_6_Run.run(b)

      println("HERE FINISH")
    }
  }