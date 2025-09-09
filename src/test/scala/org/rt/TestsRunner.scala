package org.rt

import org.rt.RunMock.{InputMock, PrintMock, RunMock, RunMocks}
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Public.{Tok, tokenize}
import org.rt.lang.RtLib_3_Parse.{Expr, fullParse, parse}
import org.rt.lang.RtLib_4_Lint.{Linted, lint}
import org.rt.lang.{RtLib_5_Build, RtLib_6_Run}
import org.rt.lang.RtLib_5_Build.Public.{Brick, BrickInput, BrickPrint, Built, BuiltRio, BuiltStr, BuiltUnit}
import org.rt.utils.RtFail.rtFail
import zio.{Ref, UIO, ZIO}
import zio.test.*

trait RtTestCase {
  final val name = this.getClass.getSimpleName

  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val linted_3: Linted
  val mb_mock_4: List[RunMocks]
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      allTestCases.map(testCase => test("tokenize " + testCase.name) {
        assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
      })
        ++ allTestCases.map(testCase => test("parse " + testCase.name) {
        assertTrue(parse(testCase.tokens_1) == testCase.expr_2)
      })
        ++ allTestCases.map(testCase => test("lint " + testCase.name) {
        assertTrue(lint(testCase.expr_2) == testCase.linted_3)
      })
        ++ allTestCases.flatMap(testCase =>
        testCase.mb_mock_4.zipWithIndex.map((mock_4, i) => test(s"run ${testCase.name} $i") {
          for {
            ref <- Ref.make(mock_4)
            brickRunner = BrickRunner.test(ref)(_)
            built = RtLib_5_Build.Public.build(testCase.linted_3, brickRunner)
            _ <- RtLib_6_Run.run(built)
            leftMockedCalls <- ref.get
          } yield assertTrue(leftMockedCalls.mockedCalls.isEmpty)
        })
      )
        ++ customTests
    )

  //todo - add custom tests
  def customTests =
    List(
      test("sync_typs"){
        val (fC, _) = (T_A0 tTo T_Unit).concretizeAsFunc(T_Str)
        assertTrue(fC == (T_Str tTo T_Unit))
      },
      test("method_syntax_1") {
        val parsedAsMethod = fullParse("a.b")
        assertTrue(parsedAsMethod == fullParse("b(a)"))
      },
      test("method_syntax_2") {
        val parsedAsMethod = fullParse("a.+(b).+(c).+(d)")
        assertTrue(parsedAsMethod == fullParse("+(d)(+(c)(+(b)(a)))"))
      },
      test("method_syntax_3") {
        val parsedAsMethod = fullParse("f0(r0)(l0).f1(r1)(l1).f2(r2)(l2)")
        assertTrue(parsedAsMethod == fullParse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))"))
      },
    )
}

private object BrickRunner {
  def test(
    mockedCallsRef: Ref[RunMocks]
  )(
    brick: Brick,
  ): BuiltRio =
    BuiltRio(
      for {
        mockedCalls <- mockedCallsRef.get
        (result, restMockedCalls) =
          (brick, mockedCalls.mockedCalls) match {
            case (BrickInput, InputMock(value) :: restMockedCalls) =>
              (BuiltStr(value), restMockedCalls)

            case (BrickPrint(s), PrintMock(value) :: restMockedCalls)
              if s == value =>
              (BuiltUnit(()), restMockedCalls)

            case _ => rtFail("runtime")
          }
        _ <- mockedCallsRef.set(RunMocks(restMockedCalls))
      } yield result
    )
}