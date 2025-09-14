package org.rt

import org.rt.lang.RtLib_5_Run.{fullRun, interactive}
import zio.*
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

val myPort = 8080
val myTaskTimeToLive = 100.seconds

type Code = String
type TaskId = String
type Line = String
type Console = Vector[Line]

type Message = String

type MyResponse = String

case class TaskState(
  consoleRef: Ref[Console],
  inputQueue: Queue[Line],
  task: Fiber[Nothing, Unit],
  expiredAt: java.time.Instant,
)

object MainWeb extends ZIOAppDefault:

  def log(
    message: String,
  ): UIO[Unit] =
    for {
      _ <- ZIO.log(message)
    } yield ()

  def run =
    for {
      runningTasks <- Ref.make(Map.empty[TaskId, TaskState])
      _ <- cleanForever(runningTasks).forkDaemon

      routes =
        Routes(
          Endpoint(RoutePattern.GET / "run_then_fetch_console")
            // http://localhost:8080/run_then_fetch_console?code=input
            // http://localhost:8080/run_then_fetch_console?code=print(%221%22)
            // http://localhost:8080/run_then_fetch_console?code=print(%22Enter%22)%0An%3C%2Dinput%0Aprint(%22Got%20%22%2Bn)
            .query(HttpCodec.query[Code]("code"))
            .out[MyResponse]
            .implement(code =>
              log(s"Code: `$code`") *>
              runThenFetchConsole(runningTasks)(code)
                .map(
                  (taskId, console) => s"""{"taskId": "$taskId", "console": "$console"}""",
                )
            ),
          Endpoint(RoutePattern.GET / "fetch_console")
            // http://localhost:8080/fetch_console?task_id=eea1acac8e024c5ea0b95ec64633dd98
            .query(HttpCodec.query[TaskId]("task_id"))
            .out[MyResponse]
            .implement(taskId =>
              fetchConsole(runningTasks)(taskId)
                .mapBoth(
                  error => s"""{"error": "$error"}""",
                  console => s"""{"console": "$console"}""",
                ).merge
            ),
          Endpoint(RoutePattern.GET / "feed_input_then_fetch_console")
            // http://localhost:8080/feed_input_then_fetch_console?task_id=eea1acac8e024c5ea0b95ec64633dd98&input_line=12
            .query(HttpCodec.query[TaskId]("task_id"))
            .query(HttpCodec.query[Line]("input_line"))
            .out[MyResponse]
            .implement((taskId, inputLine) =>
              feedInputThenFetchConsole(runningTasks)(taskId, inputLine)
                .mapBoth(
                  error => s"""{"error": "$error"}""",
                  console => s"""{"console": "$console"}""",
                ).merge
            ),
        )

      _ <- Server.serve(routes).provide(Server.defaultWithPort(myPort))
    } yield ()


  def runThenFetchConsole(
    runningTasks: Ref[Map[TaskId, TaskState]],
  )(
    code: Code,
  ): UIO[(TaskId, Console)] =
    for {
      taskId <- ZIO.succeed(java.util.UUID.randomUUID().toString.replace("-",""))
      consoleRef <- Ref.make(Vector.empty[Line])
      inputQueue <- Queue.unbounded[Line]
      interactiveBrickRunner = interactive(consoleRef, inputQueue)(_)
      task <- fullRun(code, interactiveBrickRunner)
        .unit
        .catchAll { fail => log(s"task $taskId failed: $fail") }
        .forkDaemon
      now <- ZIO.clock.flatMap(_.instant)
      _ <- runningTasks.update(
        _.updated(taskId, TaskState(consoleRef, inputQueue, task, now.plus(myTaskTimeToLive)))
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
      _ <- log(s"Currently `${runningTasks.size}` tasks.")
      shuffledKeys <- ZIO.random.flatMap(_.shuffle(runningTasks.toSeq))
      now <- ZIO.clock.flatMap(_.instant)
      _ <- ZIO.foreachDiscard(shuffledKeys.headOption) { (taskId, taskState) =>
        ZIO.when(now.isAfter(taskState.expiredAt)) {
          for {
            _ <- taskState.task.interrupt
            _ <- runningTasksRef.update(_.removed(taskId))
          } yield ()
        }
      }
      _ <- ZIO.clock.flatMap(_.sleep(myTaskTimeToLive))
    } yield ()).forever

