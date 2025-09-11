package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit, fullLint}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, rt_try, wip}
import zio.{UIO, ZIO}

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

    case class BuiltRio(run: UIO[Built]) extends Built

    case class BuiltLambda(f: Built => Built) extends Built

    sealed trait Brick

    case object BrickInput extends Brick

    case class BrickPrint(s: Str) extends Brick

    type BRICK_RUNNER = Brick => BuiltRio

    def build[A](
      typed: Linted,
      brickRunner: BRICK_RUNNER,
    ): Built =
      Internal.buildWithArgStack(
        typed,
        lambArgStack = Map.empty,
        brickRunner,
      )

    def fullBuild(
      code: String,
      brickRunner: BRICK_RUNNER,
    ) =
      build(fullLint(code), brickRunner)
  }

  private object Internal {
    object Built {
      def input(
        brickRunner: BRICK_RUNNER,
      ) =
        brickRunner(BrickInput)

      def print(
        brickRunner: BRICK_RUNNER,
      ) =
        BuiltLambda {
          case BuiltStr(s) => brickRunner(BrickPrint(s))
          case _ => rtFail("runtime")
        }

      val flatmap =
        BuiltLambda(_a_fb =>
          BuiltLambda(_fa =>
            (_a_fb, _fa) match {
              case (BuiltLambda(a_fb), BuiltRio(fa)) =>
                BuiltRio(
                  fa.flatMap { a =>
                    a_fb(a) match {
                      case BuiltRio(run) => run
                      case _ => rtFail("runtime")
                    }
                  }
                )
              case _ => rtFail("runtime")
            }
          )
        )

      val pure = BuiltLambda(a => BuiltRio(ZIO.succeed(a)))

      def plus(
        typ: Typ,
      ) =
        if (typ == (T_Str tTo (T_Str tTo T_Str)))
          BuiltLambda(_right => BuiltLambda(_left =>
            (_left, _right) match {
              case (BuiltStr(leftS), BuiltStr(rightS)) => BuiltStr(leftS + rightS)
              case _ => rtFail("runtime")
            }
          ))
        else if (typ == (T_Bint tTo (T_Bint tTo T_Bint)))
          BuiltLambda(_right => BuiltLambda(_left =>
            (_left, _right) match {
              case (BuiltBint(leftI), BuiltBint(rightI)) => BuiltBint(leftI + rightI)
              case _ => rtFail("runtime")
            }
          ))
        else rtFail("runtime", s"Unexpected typ `$typ` for `+`.")

      def minus(
        typ: Typ,
      ) =
        if (typ == (T_Bint tTo (T_Bint tTo T_Bint)))
          BuiltLambda(_right => BuiltLambda(_left =>
            (_left, _right) match {
              case (BuiltBint(leftI), BuiltBint(rightI)) => BuiltBint(leftI - rightI)
              case _ => rtFail("runtime")
            }
          ))
        else if (typ == (T_Bint tTo T_Bint))
          BuiltLambda{
            case BuiltBint(i) => BuiltBint(-i)
            case _ => rtFail("runtime")
          }
        else rtFail("runtime", s"Unexpected typ `$typ` for `-`.")

      //            case_multiply=lambda: (
      //                f"(lambda {_right}: lambda {_left}: {_left} * {_right})"
      //                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
      //                fail(f"Unexpected typ `{typ}` for `{s}`.")
      //            ),

      def str(
        typ: Typ,
      ) =
        if (typ == (T_Bint tTo T_Str))
          BuiltLambda {
            case BuiltBint(i) => BuiltStr(i.toString)
            case _ => rtFail("runtime")
          }
        else if (typ == (T_Str tTo T_Str))
          BuiltLambda {
            case BuiltStr(s) => BuiltStr(s)
            case _ => rtFail("runtime")
          }
        else if (typ == (T_Bool tTo T_Str))
          BuiltLambda {
            case BuiltBool(b) => BuiltStr(b.toString)
            case _ => rtFail("runtime")
          }
        else rtFail("runtime", s"Unexpected typ `$typ` for `str`.")


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

    def build_str_py_idf(
      s: String,
      typ: Typ,
      lambArgStack: Map[String, Built],
      brickRunner: BRICK_RUNNER,
    ): Built =
      lambArgStack.getOrElse(
        s, //todo contemplate to_latin_idf(s)
        match_builtin_idf(
          case_input = () => Built.input(brickRunner),
          case_print = () => Built.print(brickRunner),
          case_flatmap = () => Built.flatmap,
          case_pure = () => Built.pure,
          case_plus = () => Built.plus(typ),
          case_minus = () => Built.minus(typ),
          case_multiply = () => ???,
          case_str = () => Built.str(typ),
          case_true = () => ???,
          case_false = () => ???,
          case_eq_eq = () => ???,
        )(s),
      )

    def buildScalaLambda1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lambArgStack: Map[String, Built],
      brickRunner: BRICK_RUNNER,
    ): BuiltLambda =
      t_idf_x.typ match {
        case T_Unit => BuiltLambda {
          case BuiltUnit(u) =>
            buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltUnit(u)), brickRunner)
          case _ => rtFail("runtime")
        }
        case T_Str => BuiltLambda {
          case BuiltStr(s) =>
            buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltStr(s)), brickRunner)
          case _ => rtFail("runtime")
        }
        case T_Bint => BuiltLambda {
          case BuiltBint(i) =>
            buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltBint(i)), brickRunner)
          case _ => rtFail("runtime")
        }
        case T_Bool => BuiltLambda {
          case BuiltBool(b) =>
            buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltBool(b)), brickRunner)
          case _ => rtFail("runtime")
        }

        case Typ1(`builtin_RIO`, _) =>
          BuiltLambda {
            case BuiltRio(run) =>
              buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltRio(run)), brickRunner)
            case _ => rtFail("runtime")
          }

        case Typ2(`builtin_Func`, _, _) =>
          BuiltLambda {
            case BuiltLambda(f) =>
              buildWithArgStack(typed_res, lambArgStack.updated(t_idf_x.s, BuiltLambda(f)), brickRunner)
            case _ => rtFail("runtime")
          }

        case Unk0(_i) =>
          wip("Can't build generic yet")

        case _ => rtFail(s"can't build: arg `$t_idf_x` has unexpected type `${t_idf_x.typ}`")
      }

    def buildScalaCall1(
      typed_f: Linted,
      typed_x: Linted,
      lambArgStack: Map[String, Built],
      brickRunner: BRICK_RUNNER,
    ): Built = {
      val shown_f = buildWithArgStack(typed_f, lambArgStack, brickRunner)
      val shown_x = buildWithArgStack(typed_x, lambArgStack, brickRunner)

      shown_f match {
        case BuiltLambda(f) => f(shown_x)
        case _ => rtFail("runtime")
      }
    }

    def buildWithArgStack[A](
      typed: Linted,
      lambArgStack: Map[String, Built],
      brickRunner: BRICK_RUNNER,
    ): Built =
      typed match {
        case LintedLit(s, typ) =>
          buildScLit(s, typ)

        case LintedIdf(s, typ) =>
          build_str_py_idf(s, typ, lambArgStack, brickRunner)

        case LintedCall1(linted_f, linted_x, typ) =>
          buildScalaCall1(linted_f, linted_x, lambArgStack, brickRunner)

        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          buildScalaLambda1(linted_idf_x, linted_res, lambArgStack, brickRunner)
      }
  }
}
