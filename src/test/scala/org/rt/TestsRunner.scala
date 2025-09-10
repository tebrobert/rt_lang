package org.rt

import org.rt.RunMock.{InputMock, PrintMock, RunMock, RunMocks}
import org.rt.allTests.TestCase11
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Public.{Tok, TokDot, TokEndl, TokEq, TokEqGr, TokIdf, TokLessMinus, TokLitStr, TokParenClose, TokParenOpen, tokenize}
import org.rt.lang.RtLib_3_Parse.{Expr, ExprBraced, ExprIdf, fullParse, get_lines_reversed, parse, preparse_braced}
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
      test("sync_typs 1"){
        val (fC, _) = (T_A0 tTo T_Unit).concretizeAsFunc(T_Str)
        assertTrue(fC == (T_Str tTo T_Unit))
      },
      test("sync_typs 2"){
        val (fC, _) = T_A0.concretizeAsFunc(T_Str)
        assertTrue(fC == (T_Str tTo T_A0))
      },
      test("lines_reversed"){
        val code =
          """greeting = "Hey! What is your name?"
            |print(greeting)
            |name <- input
            |print("Welcome, ...")
            |print(name)
            |""".stripMargin

        val tokens = tokenize(code)
        val ext_tokens_reversed = (TokEndl +: tokens).reverse
        val raw_lines_reversed = get_lines_reversed(ext_tokens_reversed, Nil, Nil)
        val actual_lines_reversed = raw_lines_reversed.filter(_.nonEmpty)
        val expected_lines_reversed =
          List(
            List(TokIdf("print"), TokParenOpen, TokIdf("name"), TokParenClose),
            List(TokIdf("print"), TokParenOpen, TokLitStr("Welcome, ..."), TokParenClose),
            List(TokIdf("name"), TokLessMinus, TokIdf("input")),
            List(TokIdf("print"), TokParenOpen, TokIdf("greeting"), TokParenClose),
            List(TokIdf("greeting"), TokEq, TokLitStr("Hey! What is your name?")),
        )

        assertTrue(actual_lines_reversed == expected_lines_reversed)
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
      test("preparse_braced 1.1"){
        assertTrue(preparse_braced(List.empty, Nil) == List.empty)
      },
      test("preparse_braced 1.2"){
        assertTrue(preparse_braced(List(TokDot), Nil) == List(TokDot))
      },
      test("preparse_braced 1.3"){
        assertTrue(preparse_braced(List(TokDot, TokEqGr), Nil) == List(TokDot, TokEqGr))
      },
      test("preparse_braced 2"){
        val actual = preparse_braced(List(TokParenOpen, TokIdf("+"), TokParenClose), Nil)
        assertTrue(actual == List(ExprBraced(ExprIdf("+"))))
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