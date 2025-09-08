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
    for {
      _ <- ZIO.succeed(println("HERE START"))

      brickRunner = (brick: Brick) =>
          brick match {
            case BrickInput => BuiltRio(ZIO.succeed(BuiltStr(scala.io.StdIn.readLine())))
            case BrickPrint(s) => BuiltRio(ZIO.succeed(BuiltUnit(println(s))))
          }

      code =
        """s <- input
          |print(s)
          |print(s)
          |""".stripMargin

      b = fullBuild(code, brickRunner)
      _ <- org.rt.lang.RtLib_6_Run.run(b)

      _ <- ZIO.succeed(println("HERE FINISH"))
    } yield ()
  }