package org.rt

import org.rt.RunMock.{InputMock, PrintMock, RunMocks}
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Str, T_Unit, tTo}
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_3_Parse.{Expr, ExprBraced, ExprCall1, ExprIdf, ExprLitStr, fullParse, get_lines_reversed, parse, preparse_braced, preparse_call}
import org.rt.lang.RtLib_4_Lint.{Linted, fullLint, lint}
import org.rt.lang.{RtLib_5_Build, RtLib_6_Run}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, rt_try}
import org.rt.utils.RtList.rt_assert_at_least_1
import zio.Ref
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
          buildRunMocking(testCase.linted_3, mock_4)
        })
      )
        ++ customTests
    )

  //todo - add custom tests
  def customTests =
    List(
      test("sync_typs 1") {
        val (fC, _) = (T_A0 tTo T_Unit).concretizeAsFunc(T_Str)
        assertTrue(fC == (T_Str tTo T_Unit))
      },
      test("sync_typs 2") {
        val (fC, _) = T_A0.concretizeAsFunc(T_Str)
        assertTrue(fC == (T_Str tTo T_A0))
      },
      test("lines_reversed") {
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
      test("preparse_braced 1.1") {
        assertTrue(preparse_braced(List.empty, Nil) == List.empty)
      },
      test("preparse_braced 1.2") {
        assertTrue(preparse_braced(List(TokDot), Nil) == List(TokDot))
      },
      test("preparse_braced 1.3") {
        assertTrue(preparse_braced(List(TokDot, TokEqGr), Nil) == List(TokDot, TokEqGr))
      },
      test("preparse_braced 2") {
        val actual = preparse_braced(List(TokParenOpen, TokIdf("+"), TokParenClose), Nil)
        assertTrue(actual == List(ExprBraced(ExprIdf("+"))))
      },
      // todo - to not bind to Scala's `match`
      //test("test_match_token_1"){
      //  //str_idf = "idf"
      //  //str_otherwise = "otherwise"
      //  //token_s = match_token(
      //  //  case_idf=lambda s: s,
      //  //  otherwise=lambda: str_otherwise,
      //  //)(TokenIdf(str_idf))
      //  //rt_assert_equal(token_s, str_idf)
      //},
      //test("test_match_token_2"){
      //  //str_idf = "idf"
      //  //str_otherwise = "otherwise"
      //  //token_s = match_token(
      //  //  case_idf=lambda s: s,
      //  //  otherwise=lambda: str_otherwise,
      //  //)(TokenLitStr(str_idf))
      //  //rt_assert_equal(token_s, str_otherwise)
      //},
      test("assignment") {
        fullRunMocking(
          s"""msg = "hi"\nprint(msg)""",
          RunMocks(List(PrintMock("hi"))),
        )
      },
      test("flatmap_input 1") {
        fullRunMocking(
          s"""p = print("b")\np""",
          RunMocks(List(PrintMock("b"))),
        )
      },
      test("operator_naming 1") {
        fullRunMocking(
          s"""<<<~~~>>> = "Hello"\nprint(<<<~~~>>>)""",
          RunMocks(List(PrintMock("Hello"))),
        )
      },
      test("operator_naming 2") {
        fullRunMocking("" +
          s"""|||+++||| = "Hi!"\n""" +
          s"""print(|||+++|||)\n""" +
          s"""~~~ = name => "Welcome, ".+(name).+("!")\n""" +
          s"""print("Joe".~~~)""",
          RunMocks(List(PrintMock("Hi!"), PrintMock("Welcome, Joe!"))),
        )
      },
      test("rt_assert_at_least_1") {
        assertTrue(rt_try(() => rt_assert_at_least_1(List(()))).isRight)
      },
      test("new_preparse_call") {
        val exprs = List(
          ExprIdf("f"),
          ExprBraced(ExprLitStr("x")),
          ExprBraced(ExprLitStr("y")),
        )
        val res = preparse_call(exprs, Nil) // todo - remove Nils after resignaturing
        assertTrue(res == List(
          ExprCall1(
            ExprCall1(ExprIdf("f"), ExprBraced(ExprLitStr("x"))),
            ExprBraced(ExprLitStr("y"))
          ),
        ))
      },
      test("parse_with_preparse_4 - 1") {
        val tokens =
          List(
            TokIdf("f"),
            TokParenOpen, TokLitStr("x"), TokParenClose,
            TokParenOpen, TokLitStr("y"), TokParenClose,
          )

        assertTrue(rt_try(() => parse(tokens)).isRight)
      },
      test("parse_with_preparse_4 - 2") {
        assertTrue(rt_try(() => fullParse("""f("y")""")).isRight)
      },
      test("parse_with_preparse_4 - 3") {
        assertTrue(rt_try(() => fullParse("""print(+("y")("x"))""")).isRight)
      },
      test("assignment_lambdas 1") {
        fullRunMocking(
          s"""f1 <- pure(+("1"))\nprint("0".f1)""",
          RunMocks(List(PrintMock("01"))),
        )
      },
      test("assignment_lambdas 2") {
        fullRunMocking(
          s"""f1 = x => x.+("1")\nprint("0".f1)""",
          RunMocks(List(PrintMock("01"))),
        )
      },
      test("assignment_lambdas 3") {
        fullRunMocking(
          s"""f1 = +("1")\nprint("0".f1)""",
          RunMocks(List(PrintMock("01"))),
        )
      },
    )

  def fullRunMocking(
    code: String,
    mockedCalls: RunMocks,
  ) = {
    val linted = fullLint(code)
    buildRunMocking(linted, mockedCalls)
  }

  def buildRunMocking(
    linted: Linted,
    mockedCalls: RunMocks,
  ) =
    for {
      ref <- Ref.make(mockedCalls)
      brickRunner = BrickRunner.mocking(ref)(_)
      built = build(linted, brickRunner)
      _ <- RtLib_6_Run.run(built)
      leftMockedCalls <- ref.get
    } yield assertTrue(leftMockedCalls.mockedCalls.isEmpty)
}

private object BrickRunner {
  def mocking(
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