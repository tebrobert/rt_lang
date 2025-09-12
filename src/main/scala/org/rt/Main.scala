package org.rt

import org.rt.lang.RtLib_1_Tokenize.Public.tokenize
import org.rt.lang.RtLib_4_Build.Public
import org.rt.lang.RtLib_4_Build.Public.{Brick, BrickInput, BrickPrint, Built, BuiltRio, BuiltStr, BuiltUnit, fullBuild}
import org.rt.utils.RtFail.rtFail
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    for {
      _ <- ZIO.unit

      brickRunner = (brick: Brick) =>
          brick match {
            case BrickInput => BuiltRio(ZIO.succeed(BuiltStr(scala.io.StdIn.readLine())))
            case BrickPrint(s) => BuiltRio(ZIO.succeed(BuiltUnit(println(s))))
          }

      code =
        """nameRequest = "What is your name?"
          |print(nameRequest)
          |name <- input
          |print("Dear " + name + ", welcome!")
          |""".stripMargin

      b = fullBuild(code, brickRunner)
      _ <- b match {
        case Left(fail) => ZIO.succeed(println(fail))
        case Right(built) => org.rt.lang.RtLib_5_Run.run(built)
      }
    } yield ()
  }