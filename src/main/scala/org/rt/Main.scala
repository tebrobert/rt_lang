package org.rt

import org.rt.lang.RtLib_2_Tokenize.Public.tokenize
import zio.*
import zio.Console.printLine

import java.io.IOException

object Main extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] = {
    ZIO.succeed(org.rt.lang.RtLib_5_Build.test8)
  }