package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, interactive}
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.endpoint.openapi.OpenAPI.SecurityScheme.Http

import java.time.temporal.ChronoUnit.SECONDS

type Code = String
type TaskId = java.util.UUID
type Line = String
type Console = Vector[Line]

type Message = String

type MyResponse = String

case class TaskState(
  consoleRef: Ref[Console],
  inputQueue: Queue[Line],
  task: Fiber[Nothing, Unit],
  created: java.time.Instant,
)

object MainWeb extends ZIOAppDefault:

  def log(
    message: String,
  ): UIO[Unit] =
    for {
//      now <- ZIO.clock.flatMap(_.instant)
//      _ <- ZIO.succeed(println(s"$now: $message"))

      _ <- ZIO.log(message)
    } yield ()

  def run =
    for {
      runningTasks <- Ref.make(Map.empty[TaskId, TaskState])
      _ <- cleanForever(runningTasks).fork

      routes =
        Routes(
          Endpoint(RoutePattern.GET / "run_then_fetch_console")
            // http://localhost:8080/run_then_fetch_console?code=input
            .query(HttpCodec.query[Code]("code"))
            .out[MyResponse]
            .implement(code =>
              runThenFetchConsole(runningTasks)(code)
                .map(
                  (taskId, console) => s"""{"taskId": "$taskId", "console": "$console"}""",
                )
            ),
          Endpoint(RoutePattern.GET / "fetch_console")
            // http://localhost:8080/fetch_console?task_id=ae017c62-14b7-496d-95ae-b0c09a11c6b4
            .query(HttpCodec.query[TaskId]("task_id"))
            .out[MyResponse]
            .implement(taskId =>
              fetchConsole(runningTasks)(taskId)
                .mapBoth(
                  error => s"""{"error": "$error"}""",
                  console => s"""{"console": "$console"}""",
                ).merge
            ),
        )

      _ <- Server.serve(routes).provide(Server.defaultWithPort(8080))
    } yield ()


  def runThenFetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    code: Code,
  ): UIO[(TaskId, Console)] =
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
      _ <- log(s"Created task $taskId")
      console <- consoleRef.get
    } yield (taskId, console)

  def fetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    taskId: TaskId,
  ): IO[Message, Console] =
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
  ): IO[Message, Console] =
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
        ZIO.when(now.until(taskState.created, SECONDS) > 100) {
          for {
            _ <- taskState.task.interrupt
            _ <- runningTasksRef.update(_.removed(taskId))
          } yield ()
        }
      }
      _ <- ZIO.clock.flatMap(_.sleep(10.seconds))
    } yield ()).forever

