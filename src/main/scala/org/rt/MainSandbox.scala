package org.rt

import org.rt.lang.RtLib_4_Build.Public.*
import org.rt.lang.RtLib_5_Run.liveBrickRunner
import zio.*

import java.io.IOException

object MainSandbox extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    ZIO.fromEither(fullBuild(
      """inputRequest = "Type anything:"
        |print(inputRequest)
        |input
        |print("Thank you!")
        |""".stripMargin,
      liveBrickRunner,
    ))
      .flatMap(org.rt.lang.RtLib_5_Run.run(_).unit)
      .catchAll { fail => ZIO.succeed(println(fail)) }
