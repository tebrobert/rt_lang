package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, liveBrickRunner}
import zio.*

import java.io.IOException

object MainSandbox extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    fullRun(
      """print("Type anything:")
        |input
        |inputo
        |print("Thank you!")
        |""".stripMargin,
      liveBrickRunner,
    ).unit
      .catchAll { fail => ZIO.succeed(println(fail)) }
