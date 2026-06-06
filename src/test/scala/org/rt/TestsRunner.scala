package org.rt

import org.rt.RuntimeMock.{InputMock, PrintMock, RuntimeMock}
import org.rt.lang.RtLib_0_2_Builtins.{
  T_A0,
  T_Bint,
  T_Bool,
  T_RIO,
  T_Str,
  T_Unit,
  tTo,
}
import org.rt.lang.RtLib_1_Tokenize.Public.{
  Tok,
  TokDot,
  TokEndl,
  TokEq,
  TokEqGr,
  TokIdf,
  TokLessMinus,
  TokLitStr,
  TokParenClose,
  TokParenOpen,
  tokenize,
}
import org.rt.lang.RtLib_2_Parse.{
  Expr,
  ExprBraced,
  ExprCall1,
  ExprIdf,
  ExprLitBint,
  ExprLitStr,
  fullParse,
  get_lines_reversed,
  parse,
  preparse_braced,
  preparse_call,
}
import org.rt.lang.RtLib_3_Lint.{
  Linted,
  LintedCall1,
  LintedIdf,
  LintedLit,
  fullLint,
  lint,
  lint_set,
}
import org.rt.lang.RtLib_4_Build.Public.{
  Brick,
  BrickInput,
  BrickPrint,
  BuiltRio,
  BuiltStr,
  BuiltUnit,
  build,
  fullBuild,
}
import org.rt.lang.RtLib_5_Run
import org.rt.utils.RtFail.{rtFailUnsafe, rt_try}
import org.rt.utils.RtList.rt_assert_at_least_1
import zio.test.{Spec, ZIOSpecDefault, assertTrue}
import zio.{Ref, ZIO}

trait RtTestCase {
  final val name = this.getClass.getSimpleName

  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val linted_3: Linted
  val mb_mock_4: List[List[RuntimeMock]]
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      allTestCases.map(testCase =>
        test("tokenize " + testCase.name) {
          assertTrue(tokenize(testCase.code_0) == Right(testCase.tokens_1))
        },
      )
        ++ allTestCases.map(testCase =>
          test("parse " + testCase.name) {
            assertTrue(parse(testCase.tokens_1) == testCase.expr_2)
          },
        )
        ++ allTestCases.map(testCase =>
          test("lint " + testCase.name) {
            assertTrue(lint(testCase.expr_2) == testCase.linted_3)
          },
        )
        ++ allTestCases.flatMap(testCase =>
          testCase.mb_mock_4.zipWithIndex.map((mock_4, i) =>
            test(s"run ${testCase.name} $i") {
              buildRunMocking(testCase.linted_3, mock_4)
                .catchAll(fail =>
                  ZIO.succeed(println(fail)).as(assertTrue(false)),
                )
            },
          ),
        )
        ++ customTests,
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

        val eiTokens = tokenize(code)

        eiTokens match {
          case Left(fail) => assertTrue(false)
          case Right(tokens) =>
            val ext_tokens_reversed = (TokEndl +: tokens).reverse
            val raw_lines_reversed =
              get_lines_reversed(ext_tokens_reversed, Nil, Nil)
            val actual_lines_reversed = raw_lines_reversed.filter(_.nonEmpty)
            val expected_lines_reversed =
              List(
                List(
                  TokIdf("print"),
                  TokParenOpen,
                  TokIdf("name"),
                  TokParenClose,
                ),
                List(
                  TokIdf("print"),
                  TokParenOpen,
                  TokLitStr("Welcome, ..."),
                  TokParenClose,
                ),
                List(TokIdf("name"), TokLessMinus, TokIdf("input")),
                List(
                  TokIdf("print"),
                  TokParenOpen,
                  TokIdf("greeting"),
                  TokParenClose,
                ),
                List(
                  TokIdf("greeting"),
                  TokEq,
                  TokLitStr("Hey! What is your name?"),
                ),
              )

            assertTrue(actual_lines_reversed == expected_lines_reversed)
        }
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
        assertTrue(
          parsedAsMethod == fullParse("f2(r2)(l2)(f1(r1)(l1)(f0(r0)(l0)))"),
        )
      },
      test("preparse_braced 1.1") {
        assertTrue(preparse_braced(List.empty, Nil) == List.empty)
      },
      test("preparse_braced 1.2") {
        assertTrue(preparse_braced(List(TokDot), Nil) == List(TokDot))
      },
      test("preparse_braced 1.3") {
        assertTrue(
          preparse_braced(List(TokDot, TokEqGr), Nil) == List(TokDot, TokEqGr),
        )
      },
      test("preparse_braced 2") {
        val actual =
          preparse_braced(List(TokParenOpen, TokIdf("+"), TokParenClose), Nil)
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
      test("simple print") {
        fullRunMocking(
          s"""print("q")""",
          List(PrintMock("q")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("simple input-print") {
        fullRunMocking(
          s"""x <- input\nprint(x)""",
          List(InputMock("q"), PrintMock("q")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("assignment") {
        fullRunMocking(
          s"""msg = "hi"\nprint(msg)""",
          List(PrintMock("hi")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("flatmap_input 1") {
        fullRunMocking(
          s"""p = print("b")\np""",
          List(PrintMock("b")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("operator_naming 1") {
        fullRunMocking(
          s"""<<<~~~>>> = "Hello"\nprint(<<<~~~>>>)""",
          List(PrintMock("Hello")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("operator_naming 2") {
        fullRunMocking(
          "" +
            s"""|||+++||| = "Hi!"\n""" +
            s"""print(|||+++|||)\n""" +
            s"""~~~ = name => "Welcome, ".+(name).+("!")\n""" +
            s"""print("Joe".~~~)""",
          List(PrintMock("Hi!"), PrintMock("Welcome, Joe!")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
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
        val res =
          preparse_call(exprs, Nil) // todo - remove Nils after resignaturing
        assertTrue(
          res == List(
            ExprCall1(
              ExprCall1(ExprIdf("f"), ExprBraced(ExprLitStr("x"))),
              ExprBraced(ExprLitStr("y")),
            ),
          ),
        )
      },
      test("parse_with_preparse_4 - 1") {
        val tokens =
          List(
            TokIdf("f"),
            TokParenOpen,
            TokLitStr("x"),
            TokParenClose,
            TokParenOpen,
            TokLitStr("y"),
            TokParenClose,
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
          List(PrintMock("01")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("assignment_lambdas 2") {
        fullRunMocking(
          s"""f1 = x => x.+("1")\nprint("0".f1)""",
          List(PrintMock("01")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("assignment_lambdas 3") {
        fullRunMocking(
          s"""f1 = +("1")\nprint("0".f1)""",
          List(PrintMock("01")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("integers") {
        assertTrue(fullParse("\n1\n") == Right(ExprLitBint("1")))
      },
      test("integers_printing") {
        fullRunMocking(
          s"""num = 1\nprint("The number is " + str(num))""",
          List(PrintMock("The number is 1")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("integers_plus") {
        fullRunMocking(
          s"""num = 1 + 2\nprint("The number is " + str(num))""",
          List(PrintMock("The number is 3")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("integers_unary_minus") {
        fullRunMocking(
          s"""print(str(-2))""",
          List(PrintMock("-2")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("lint_set") {
        assertTrue(rt_try(() => lint_set(ExprIdf("num"))).isRight)
      },
      //test("test_match_list_10") { // todo - to not bind to Scala's `match`
      //  //match_list(
      //  //    case_at_least_1=lambda head, _tail: head,
      //  //    case_empty=lambda: 0,
      //  //)([])
      //},
      //test("test_match_list_2o") { // todo - to not bind to Scala's `match`
      //  //match_list(
      //  //  case_at_least_2=lambda head0, head1, tail1: head0 + head1,
      //  //  otherwise=lambda: 0,
      //  //)([])
      //},
      test("funcs 1") {
        fullRunMocking(
          s"""identity = (x => x)("")\nprint(identity)""",
          List(PrintMock("")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("funcs 2.1") { // sync types actually
        val (actual, _) =
          (T_RIO(T_Str tTo T_A0) tTo T_RIO(T_Unit))
            .concretizeAsFunc(T_RIO(T_A0 tTo T_A0))
        val expected = T_RIO(T_Str tTo T_Str) tTo T_RIO(T_Unit)
        assertTrue(actual == expected)
      },
      test("funcs 2.2") {
        fullRunMocking(
          s"""identity = (x => x)\nprint(identity(""))""",
          List(PrintMock("")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("funcs 3") {
        fullRunMocking(
          s"""f = x => x + 1\nprint("The result is " + str(f(5)))""",
          List(PrintMock("The result is 6")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("lint_set 2") {
        val eiExpr = fullParse("str(f(5))")
        eiExpr match {
          case Left(_) => assertTrue(false)
          case Right(expr) =>
            val actual = lint_set(expr)
            val expected =
              Set(
                LintedCall1(
                  LintedIdf("str", T_Bint tTo T_Str),
                  LintedCall1(
                    LintedIdf("f", T_Bint tTo T_Bint),
                    LintedLit("5", T_Bint),
                    T_Bint,
                  ),
                  T_Str,
                ),
                LintedCall1(
                  LintedIdf("str", T_Bool tTo T_Str),
                  LintedCall1(
                    LintedIdf("f", T_Bint tTo T_Bool),
                    LintedLit("5", T_Bint),
                    T_Bool,
                  ),
                  T_Str,
                ),
              )
            assertTrue(actual == expected)
        }
      },
      test("tokenize") {
        assertTrue(tokenize("==") == Right(List(TokIdf("=="))))
      },
      test("apply 1") {
        fullRunMocking(
          s"""f = x => x + ""\nprint(f(""))""",
          List(PrintMock("")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("apply 2") {
        fullRunMocking(
          s"""f = x => x + ""\nprint(f(f("")))""",
          List(PrintMock("")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("apply 3") {
        fullRunMocking(
          s"""f = +("")\nprint("".f.f)""",
          List(PrintMock("")),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      //test("apply 4") { // todo - `Can't lint`
      //  fullRunMocking(
      //    s"""f = +("")\nprint("".f.f.f)""",
      //    List(PrintMock("")),
      //  )
      //},
      test("flatmap_input 2") {
        fullRunMocking(
          s"""doAskName = print("What your name?").>>=(_ => input)
             |doGreet = name => "Hi, ".+(name).+("!").print
             |doAskName.>>=(doGreet)
             |""".stripMargin,
          List(
            PrintMock("What your name?"),
            InputMock("Tester"),
            PrintMock("Hi, Tester!"),
          ),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      //      test("flatmap_input 3") {
      //        fullRunMocking(
      //          s"""doAskName = print("What your name?").>>=(_ => input)
      //             |doGreet = name => ("Hi, " + name + "!").print
      //             |doAskName >>= (doGreet)  //todo - fails as operator
      //             |""".stripMargin,
      //          List(
      //            PrintMock("What your name?"),
      //            InputMock("Tester"),
      //            PrintMock("Hi, Tester!"),
      //          ),
      //        )
      //      },
      test("bad build") {
        assertTrue(fullBuild("", BrickRunner.empty).isLeft)
      },
      test("input as unit - 1") {
        fullRunMocking(
          s"""print("Type anything:")
             |_ <- input
             |print("Thank you!")
             |""".stripMargin,
          List(
            PrintMock("Type anything:"),
            InputMock("..."),
            PrintMock("Thank you!"),
          ),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
      test("input as unit - 2") {
        fullRunMocking(
          s"""print("Type anything:")
             |input
             |print("Thank you!")
             |""".stripMargin,
          List(
            PrintMock("Type anything:"),
            InputMock("..."),
            PrintMock("Thank you!"),
          ),
        ).catchAll(fail => ZIO.succeed(println(fail)).as(assertTrue(false)))
      },
    )

  def fullRunMocking(
      code: String,
      mockedCalls: List[RuntimeMock],
  ) = {
    val eiLinted = fullLint(code)

    eiLinted match {
      case Left(fail) =>
        ZIO.succeed {
          print(fail)
          assertTrue(false)
        }
      case Right(linted) => buildRunMocking(linted, mockedCalls)
    }
  }

  def buildRunMocking(
      linted: Linted,
      mockedCalls: List[RuntimeMock],
  ) =
    for {
      ref <- Ref.make(mockedCalls)
      brickRunner = BrickRunner.mocking(ref)(_)
      eiBuilt = build(linted, brickRunner)
      _ <- eiBuilt match {
        case Left(fail)   => ZIO.fail(fail)
        case Right(built) => RtLib_5_Run.run(built)
      }
      leftMockedCalls <- ref.get
    } yield assertTrue(leftMockedCalls.isEmpty)
}

private object BrickRunner {
  def empty(
      brick: Brick,
  ): BuiltRio =
    BuiltRio(ZIO.succeed(brick match {
      case BrickInput    => BuiltStr("")
      case BrickPrint(s) => BuiltUnit(())
    }))

  def mocking(
      mockedCallsRef: Ref[List[RuntimeMock]],
  )(  brick: Brick,
  ): BuiltRio =
    BuiltRio(
      for {
        mockedCalls <- mockedCallsRef.get
        (result, restMockedCalls) =
          (brick, mockedCalls) match {
            case (BrickInput, InputMock(value) :: restMockedCalls) =>
              (BuiltStr(value), restMockedCalls)

            case (BrickPrint(s), PrintMock(value) :: restMockedCalls)
                if s == value =>
              (BuiltUnit(()), restMockedCalls)

            case _ => rtFailUnsafe("runtime")
          }
        _ <- mockedCallsRef.set(restMockedCalls)
      } yield result,
    )
}
