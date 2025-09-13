package org.rt.lang

import org.rt.lang.RtLib_4_Build.Public.{Brick, BrickInput, BrickPrint, Built, BuiltRio, BuiltStr, BuiltUnit}
import org.rt.utils.RtFail.RtFail
import zio.{IO, ZIO}

object RtLib_5_Run {
  def run(
    built: Built,
  ): IO[RtFail, Built] =
    built match {
      case BuiltRio(run) => run
      case _ => ZIO.fail(RtFail("runtime"))
    }

  val liveBrickRunner = (brick: Brick) =>
    brick match { // todo - to not bind to Scala's `match`
      case BrickInput => BuiltRio(ZIO.succeed(BuiltStr(scala.io.StdIn.readLine())))
      case BrickPrint(s) => BuiltRio(ZIO.succeed(BuiltUnit(println(s))))
    }

}
