package org.rt

import lang.RtLib_3_Parse.{Expr, parse}
import org.rt.lang.RtLib_0_1_Types.{Typ0, Typ1, Typ2, Unk0}
import org.rt.lang.RtLib_2_Tokenize.Public.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, lint}
import zio.test.*

trait RtTestCase {
  val code_0: String
  val tokens_1: List[Tok]
  val expr_2: Expr
  val mb_linted_3: Option[Linted] = None
}

object TestsRunner extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] =
    suite("HelloWorldSpec")(
      //*
      allTestCases.map(testCase => test("tokenize " + testCase.getClass.getSimpleName) {
        assertTrue(tokenize(testCase.code_0) == testCase.tokens_1)
      })
        ++ allTestCases.map(testCase => test("parse " + testCase.getClass.getSimpleName) {
        assertTrue(parse(testCase.tokens_1) == testCase.expr_2)
      })
        ++ allTestCases.flatMap(testCase =>
        testCase.mb_linted_3.map { linted_3 =>
          test("lint " + testCase.getClass.getSimpleName) {
            assertTrue(lint(testCase.expr_2) == linted_3)
          }
        }
      )
        ++
       // */
      Seq(a)


    )

  def a =
    test("debugging bad") {
      assertTrue(
        org.rt.lang.RtLib_4_Lint.continue_linting_call_1(
          LintedCall1(
            linted_f = LintedIdf(">>=", Typ2("Func",
              Typ2("Func", Unk0(0), Typ1("RIO", Typ0("Unit"))),
              Typ2("Func", Typ1("RIO", Unk0(0)), Typ1("RIO", Typ0("Unit"))),
            )),
            linted_x = LintedLambda1(
              LintedIdf("u", Unk0(0)),
              LintedCall1(
                LintedIdf("print", Typ2("Func", Typ0("Str"), Typ1("RIO", Typ0("Unit")))),
                LintedIdf("s", Typ0("Str")),
                Typ1("RIO", Typ0("Unit")),
              ),
              Typ2("Func", Unk0(0), Typ1("RIO", Typ0("Unit"))),
            ),
            typ = Typ2("Func", Typ1("RIO", Unk0(0)), Typ1("RIO", Typ0("Unit"))),
          ),
          LintedCall1(
            LintedIdf("print", Typ2("Func", Typ0("Str"), Typ1("RIO", Typ0("Unit")))),
            LintedIdf("s", Typ0("Str")),
            Typ1("RIO", Typ0("Unit")),
          ),
        ) ==
          LintedCall1(
            LintedCall1(
              linted_f = LintedIdf(">>=", Typ2("Func",
                Typ2("Func", Unk0(0), Typ1("RIO", Typ0("Unit"))),
                Typ2("Func", Typ1("RIO", Typ0("Unit")), Typ1("RIO", Typ0("Unit"))),
              )),
              linted_x = LintedLambda1(
                LintedIdf("u", Unk0(0)),
                LintedCall1(
                  LintedIdf("print", Typ2("Func", Typ0("Str"), Typ1("RIO", Typ0("Unit")))),
                  LintedIdf("s", Typ0("Str")),
                  Typ1("RIO", Typ0("Unit")),
                ),
                Typ2("Func", Unk0(0), Typ1("RIO", Typ0("Unit"))),
              ),
              typ =Typ2("Func", Typ1("RIO", Typ0("Unit")), Typ1("RIO", Typ0("Unit"))),
            ),
            LintedCall1(
              LintedIdf("print", Typ2("Func", Typ0("Str"), Typ1("RIO", Typ0("Unit")))),
              LintedIdf("s", Typ0("Str")),
              Typ1("RIO", Typ0("Unit")),
            ),
            Typ1("RIO", Typ0("Unit")),
          )
      )
    }
}