package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit, full_lint}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, rt_try, wip}

object RtLib_5_Build {
  object Public {

    type Str = String
    type Bint = BigInt
    type Bool = Boolean

    sealed trait Built

    case class BuiltUnit(u: Unit) extends Built // convenient for Scala's side-effects
    case class BuiltStr(s: Str) extends Built
    case class BuiltBint(i: Bint) extends Built
    case class BuiltBool(b: Bool) extends Built

    case class BuiltRio(run: () => Built) extends Built
    case class BuiltLambda(f: Built => Built) extends Built





    def buildScala[A](
      typed: Linted,
    ): Built =
      Internal.buildScalaWithStacks(
        typed = typed,
        lambArgStack = Map.empty,
      )

    def fullBuildScala(code: String) =
      buildScala(full_lint(code))
  }

  private object Internal {
    object Built {
      val input =
        BuiltRio(() => BuiltStr(scala.io.StdIn.readLine())) // todo - move out to `run` for ability to mock

      val print =
        BuiltLambda {
          case BuiltStr(s) => BuiltRio(() => BuiltUnit(println(s)))
          case _ => rtFail("runtime")
        }

      val flatmap =
        BuiltLambda(_a_fb =>
          BuiltLambda(_fa =>
            (_a_fb, _fa) match {
              case (BuiltLambda(a_fb), BuiltRio(fa)) =>
                BuiltRio(() =>
                  a_fb(fa()) match {
                    case BuiltRio(run) => run()
                    case _ => rtFail("runtime")
                  }
                )
              case _ => rtFail("runtime")
            }
          )
        )

      //            case_pure=lambda: f"(lambda {_a}: {BrickPure(_a)})",
      //            case_plus=lambda: (
      //                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
      //                if typ == T_Func(T_Str, T_Func(T_Str, T_Str)) else
      //                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
      //                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            ),

      //            case_minus=lambda: (
      //                f"(lambda {_right}: lambda {_left}: {_left} - {_right})"
      //                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
      //                f"(lambda {_num}: - {_num})"
      //                if typ == T_Func(T_Bint, T_Bint) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            ),

      //            case_multiply=lambda: (
      //                f"(lambda {_right}: lambda {_left}: {_left} * {_right})"
      //                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            ),

      //            case_str=lambda: (
      //                f"(str)"
      //                if typ == T_Func(T_Bint, T_Str) else
      //                f"(str)"
      //                if typ == T_Func(T_Str, T_Str) else
      //                f"(lambda {_a}: str({_a}).lower())"
      //                if typ == T_Func(T_Bool, T_Str) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            ),

      //            case_true=lambda: "(True)",

      //            case_false=lambda: "(False)",

      //            case_eq_eq=lambda: (
      //                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
      //                if typ == T_Func(T_Str, T_Func(T_Str, T_Bool)) else
      //                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
      //                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bool)) else
      //                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
      //                if typ == T_Func(T_Bool, T_Func(T_Bool, T_Bool)) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            )
    }

    def buildScLitBint(
      serializedValue: String
    ) = {
      val bint = rt_try(() => BigInt(serializedValue))
        .getOrElse(rtFail(s"Unexpected BigInt: `$serializedValue`."))

      BuiltBint(bint)
    }


    def buildScLit(
      serializedValue: String,
      typ: Typ,
    ): Built = {
      if (typ == T_Str)
        BuiltStr(serializedValue)
      else if (typ == T_Bint)
        buildScLitBint(serializedValue)
      else rtFail(s"Unexpected literal: `$serializedValue`, `$typ`.")
    }

    def to_latin_idf: String => String = ???

    // todo - shorten
    def build_str_py_idf(
      s: String,
      typ: Typ,
      lambArgStack: Map[String, Built],
    ): Built =
      lambArgStack.getOrElse(
        s, //todo contemplate to_latin_idf(s)
        match_builtin_idf(
          case_input = () => Built.input,
          case_print = () => Built.print,
          case_flatmap = () => Built.flatmap,
          case_pure = () => ???,
          case_plus = () => ???,
          case_minus = () => ???,
          case_multiply = () => ???,
          case_str = () => ???,
          case_true = () => ???,
          case_false = () => ???,
          case_eq_eq = () => ???,
        )(s),
      )

    def buildScalaLambda1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lambArgStack: Map[String, Built],
    ): BuiltLambda =
      t_idf_x.typ match {
        case T_Unit => BuiltLambda {
          case BuiltUnit(u) =>
            buildScalaWithStacks(typed_res, lambArgStack.updated(t_idf_x.s, BuiltUnit(u)))
          case _ => rtFail("runtime")
        }
        case T_Str => BuiltLambda {
          case BuiltStr(s) =>
            buildScalaWithStacks(typed_res, lambArgStack.updated(t_idf_x.s, BuiltStr(s)))
          case _ => rtFail("runtime")
        }
        case T_Bint => BuiltLambda {
          case BuiltBint(i) =>
            buildScalaWithStacks(typed_res, lambArgStack.updated(t_idf_x.s, BuiltBint(i)))
          case _ => rtFail("runtime")
        }
        case T_Bool => BuiltLambda {
          case BuiltBool(b) =>
            buildScalaWithStacks(typed_res, lambArgStack.updated(t_idf_x.s, BuiltBool(b)))
          case _ => rtFail("runtime")
        }

        case Typ1(`builtin_RIO`, _) => wip()
        case Typ2(`builtin_Func`, _, _) => wip()
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }

    def buildScalaCall1(
      typed_f: Linted,
      typed_x: Linted,
      lambArgStack: Map[String, Built],
    ): Built = {
      val shown_f = buildScalaWithStacks(typed_f, lambArgStack)
      val shown_x = buildScalaWithStacks(typed_x, lambArgStack)

      shown_f match {
        case BuiltLambda(f) => f(shown_x)
        case _ => rtFail("runtime")
      }
    }

    def buildScalaWithStacks[A](
      typed: Linted,
      lambArgStack: Map[String, Built],
    ): Built =
      typed match {
        case LintedLit(s, typ) => buildScLit(s, typ)
        case LintedIdf(s, typ) => build_str_py_idf(
          s,
          typ,
          lambArgStack,
        )
        case LintedCall1(linted_f, linted_x, typ) => buildScalaCall1(linted_f, linted_x, lambArgStack)
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          buildScalaLambda1(
            linted_idf_x,
            linted_res,
            lambArgStack,
          )
      }
  }
}
