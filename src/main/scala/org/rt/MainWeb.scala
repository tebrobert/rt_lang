package org.rt

import zio.*

import java.io.IOException

type Code = String
type TaskId = java.util.UUID
type Line = String

case class TaskState(built: Unit, console: List[Line])

object MainWeb extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    for {
      _ <- Ref.make(Map.empty[TaskId, TaskState])
    } yield ()

  // todo - design api

  /**   /run_then_fetch_console
   *      : Code => (TaskId, List[Line])
   *
   *    /fetch_console
   *      : TaskId => List[Line]
   *
   *    /feed_input_then_fetch_console
   *      : (TaskId, Line) => List[Line]
   */
