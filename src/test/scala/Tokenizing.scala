import full.TestCase1
import lang.RtLib_2_Tokenize.tokenize
import zio.{UIO, ZIO}
import zio.test.*

object Tokenizing extends ZIOSpecDefault {
//  def readFile: UIO[String] =
//    ZIO.acquireReleaseWith(
//      ZIO.succeed(scala.io.Source.fromFile("/home/rt/Desktop/#rt_lang/src/test/resources/1/1_code.rt.txt"))
//    )(bufferedSource => ZIO.succeed(bufferedSource.close()))(
//      bufferedSource => ZIO.succeed(bufferedSource.getLines().mkString("\n"))
//    )

  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      test("1"){
        assertTrue(tokenize(TestCase1.code_0) == TestCase1.tokens_1)
      },
    )
}
