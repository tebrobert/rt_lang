package org.rt

import org.rt.lang.RtLib_4_Build.Public.*
import org.rt.lang.RtLib_5_Run.liveBrickRunner
import zio.*

import java.io.IOException

object MainSandbox extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    ZIO.fromEither(fullBuild(
      """nameRequest = "What is your name?"
        |print(nameRequest)
        |name <- input
        |print("Dear " + name + ", welcome!")
        |""".stripMargin,
      liveBrickRunner,
    ))
      .flatMap(org.rt.lang.RtLib_5_Run.run(_).unit)
      .catchAll { fail => ZIO.succeed(println(fail)) }
