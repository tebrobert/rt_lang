package org.rt

import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Lint.*
import zio.*
import zio.Console.printLine

import java.io.IOException

object Sandbox extends ZIOAppDefault:
  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    printLine(
      LintedCall1(
        LintedIdf("print", Typ0("Bint")),
        LintedIdf("true", Typ0("Bint")),
        Unk0(0),
      )
    )
