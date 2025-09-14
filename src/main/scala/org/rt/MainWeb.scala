package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, interactive}
import zio.*
import zio.http.*

import java.time.temporal.ChronoUnit.SECONDS

type Code = String
type TaskId = java.util.UUID
type Line = String

type Message = String

case class TaskState(
  consoleRef: Ref[Vector[Line]],
  inputQueue: Queue[Line],
  task: Fiber[Nothing, Unit],
  created: java.time.Instant,
)

object MainWeb extends ZIOAppDefault:

  def run =
    for {
      runningTasks <- Ref.make(Map.empty[TaskId, TaskState])
      _ <- cleanForever(runningTasks).fork

      routes =
        Routes(
          Method.GET / "hello" -> handler { (req: Request) =>
            val name = req.queryOrElse("name", "World")
            Response.text(s"Hello $name!")
          },
          Method.GET / "hi" -> handler { (req: Request) =>
            val name = req.queryOrElse("name", "World")
            Response.text(s"Hi $name!")
          },
        )

      _ <- Server.serve(routes).provide(Server.defaultWithPort(8080))
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
        .catchAll { fail => ZIO.succeed(println(s"task $taskId failed: $fail")) }
        .fork
      now <- ZIO.clock.flatMap(_.instant)
      _ <- runningTasks.update(
        _.updated(taskId, TaskState(consoleRef, inputQueue, task, now))
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

  def cleanForever(
    runningTasksRef: Ref[Map[TaskId, TaskState]],
  ): UIO[Nothing] =
    (for {
      runningTasks <- runningTasksRef.get
      shuffledKeys <- ZIO.random.flatMap(_.shuffle(runningTasks.toSeq))
      now <- ZIO.clock.flatMap(_.instant)
      _ <- ZIO.foreachDiscard(shuffledKeys.headOption) { (taskId, taskState) =>
        ZIO.when(now.until(taskState.created, SECONDS) > 100){
            for {
              _ <- taskState.task.interrupt
              _ <- runningTasksRef.update(_.removed(taskId))
            } yield ()
        }
      }
      _ <- ZIO.clock.flatMap(_.sleep(10.seconds))
    } yield ()).forever

