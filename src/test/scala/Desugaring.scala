import zio.{UIO, ZIO}
import zio.test.*

object Desugaring extends ZIOSpecDefault {
  def readFile: UIO[String] =
    ZIO.acquireReleaseWith(
      ZIO.succeed(scala.io.Source.fromFile("/home/rt/Desktop/#rt_lang/src/test/resources/1/1_code.rt.txt"))
    )(bufferedSource => ZIO.succeed(bufferedSource.close()))(
      bufferedSource => ZIO.succeed(bufferedSource.getLines().mkString("\n"))
    )

  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      test("1"){
        for {
          s <- readFile
          _ <- zio.Console.printLine(s).orDie
        } yield assertTrue(true)
      },
    )
}
