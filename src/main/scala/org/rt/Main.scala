package org.rt

import org.rt.lang.RtLib_4_Build.Public.*
import org.rt.lang.RtLib_5_Run.liveBrickRunner
import zio.*

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    fullBuild(
      """nameRequest = "What is your name?"
        |print(nameRequest)
        |name <- input
        |print("Dear " + name + ", welcome!")
        |""".stripMargin,
      liveBrickRunner,
    ) match {
      case Left(fail) => ZIO.succeed(println(fail))
      case Right(built) => org.rt.lang.RtLib_5_Run.run(built).unit
        .catchAll { fail => ZIO.succeed(println(fail)) }
    }
  }