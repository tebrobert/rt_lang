package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, interactive}
import zio.*

import java.io.IOException

type Code = String
type TaskId = java.util.UUID
type Line = String

type Message = String

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

  def runThenFetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    code: Code,
  ): UIO[(TaskId, Vector[Line])] =
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
      console <- consoleRef.get
    } yield (taskId, console)

  def fetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    taskId: TaskId,
  ): IO[Message, Vector[Line]] =
    for {
      mbTaskState <- runningTasks.get.map(_.get(taskId))
      taskState <- ZIO.fromOption(mbTaskState).orElseFail(s"No such task by given taskId: `$taskId`")
      console <- taskState.consoleRef.get
    } yield console

  def feedInputThenFetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    taskId: TaskId,
    inputLine: Line,
  ): IO[Message, Vector[Line]] =
    for {
      mbTaskState <- runningTasks.get.map(_.get(taskId))
      taskState <- ZIO.fromOption(mbTaskState).orElseFail(s"No such task by given taskId: `$taskId`")
      succeed <- taskState.inputQueue.offer(inputLine)
      _ <- ZIO.unless(succeed)(ZIO.fail("Could not provide input line."))
      console <- taskState.consoleRef.get
    } yield console
