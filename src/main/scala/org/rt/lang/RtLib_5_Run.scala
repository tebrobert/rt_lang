package org.rt.lang

import org.rt.Line
import org.rt.lang.RtLib_4_Build.Public.*
import org.rt.utils.RtFail.RtFail
import zio.{IO, Queue, Ref, ZIO}

object RtLib_5_Run {
  def run(
    built: Built,
  ): IO[RtFail, Built] =
    built match {
      case BuiltRio(run) => run
      case _ => ZIO.fail(RtFail("runtime"))
    }

  def fullRun(
    code: String,
    brickRunner: BRICK_RUNNER,
  ): IO[RtFail, Built] =
    try { //todo - try better typing
      ZIO.fromEither(fullBuild(code, brickRunner))
        .flatMap(run)
    } catch {
      case fail: RtFail => ZIO.fail(fail)
    }

  val liveBrickRunner = (brick: Brick) =>
    brick match { // todo - to not bind to Scala's `match`
      case BrickInput => BuiltRio(ZIO.succeed(BuiltStr(scala.io.StdIn.readLine())))
      case BrickPrint(s) => BuiltRio(ZIO.succeed(BuiltUnit(println(s))))
    }

  def interactive(
    consoleRef: Ref[Vector[Line]],
    inputQueue: Queue[Line],
  )(
    brick: Brick,
  ): BuiltRio =
    BuiltRio(
      brick match {
        case BrickInput =>
          for {
            s <- inputQueue.take
            _ <- consoleRef.update(_.appended(s))
          } yield BuiltStr(s)

        case BrickPrint(s) =>
          consoleRef.update(_.appended(s))
            .as(BuiltUnit(()))
      }
    )
}
