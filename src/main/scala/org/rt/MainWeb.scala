package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, interactive}
import zio.*

import java.io.IOException

type Code = String
type TaskId = java.util.UUID
type Line = String

case class TaskState(
  consoleRef: Ref[Vector[Line]],
  inputQueue: Queue[Line],
  task: Fiber[Nothing, Unit],
)

object MainWeb extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs, IOException, Unit] =
    for {
      runningTasks <- Ref.make(Map.empty[TaskId, TaskState])
    } yield ()

  def apiRun(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    code: Code,
  ): UIO[TaskId] =
    for {
      taskId <- ZIO.succeed(java.util.UUID.randomUUID())
      consoleRef <- Ref.make(Vector.empty[Line])
      inputQueue <- Queue.unbounded[Line]
      interactiveBrickRunner = interactive(consoleRef, inputQueue)(_)
      task <- fullRun(code, interactiveBrickRunner)
        .unit
        .catchAll{fail => ZIO.succeed(println(s"task $taskId failed: $fail"))}
        .fork
      _ <- runningTasks.update(
        _.updated(taskId, TaskState(consoleRef, inputQueue, task))
      )
    } yield taskId

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
