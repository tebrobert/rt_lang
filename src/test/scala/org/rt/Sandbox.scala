package org.rt

import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_2_Tokenize.*
import org.rt.lang.RtLib_3_Parse.*
import org.rt.lang.RtLib_4_Typify.*
import zio.*
import zio.Console.printLine

import java.io.IOException

object Sandbox extends ZIOAppDefault:
  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    printLine(
      TypifiedCall1(
        TypifiedIdf("print", Typ0("Bint")),
        TypifiedIdf("true", Typ0("Bint")),
        Unk0(0),
      )
    )
