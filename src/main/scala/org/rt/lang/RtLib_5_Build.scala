package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, rt_try, wip}

object RtLib_5_Build {
  object Public {
    sealed trait Brick[A]

    case object BrickInput extends Brick[Str]

    final case class BrickPrint(s: Str) extends Brick[Unit]

    final case class BrickPure[A](a: A) extends Brick[A]

    final case class BrickFlatmap[A, B](
      a_fb: A => Brick[B],
      fa: Brick[A],
    ) extends Brick[B]

    type Str = String
    type Bint = BigInt
    type Bool = Boolean

    sealed trait BuiltSc

    sealed trait BuiltScLit extends BuiltSc // Sc - Scala

    case object BuiltScLitUnit extends BuiltScLit

    case class BuiltScLitStr(s: Str) extends BuiltScLit

    case class BuiltScLitBint(i: Bint) extends BuiltScLit

    case class BuiltScLitBool(b: Bool) extends BuiltScLit

    case class BuiltScLambda(
      f: (Unit => BuiltSc)
        | (Str => BuiltSc)
        | (Bint => BuiltSc)
        | (Bool => BuiltSc)
        | (BuiltScLambda => BuiltSc)
    ) extends BuiltSc

    def buildScala[A](
      typed: Linted,
    ): BuiltSc =
      Internal.buildScalaWithStacks(
        typed = typed,
        lamb_arg_stack = List.empty, // legacy todo remove
        lambArgStackStr = List.empty,
        lambArgStackBint = List.empty,
        lambArgStackBool = List.empty,
      )
  }

  private object Internal {
    def buildScLitBint(
      serializedValue: String
    ) = {
      val bint = rt_try(() => BigInt(serializedValue))
        .getOrElse(rtFail(s"Unexpected BigInt: `$serializedValue`."))

      BuiltScLitBint(bint)
    }


    def buildScLit(
      serializedValue: String,
      typ: Typ,
    ): BuiltScLit = {
      if (typ == T_Str)
        BuiltScLitStr(serializedValue)
      else if (typ == T_Bint)
        buildScLitBint(serializedValue)
      else rtFail(s"Unexpected literal: `$serializedValue`, `$typ`.")
    }

    /*
    // todo - shorten
    def build_str_py_idf(s, typ, lamb_arg_stack) = {
    return (
        to_latin_idf(s) if s in lamb_arg_stack else
        match_builtin_idf(
            case_input=lambda: f"{BrickInput()}",
            case_print=lambda: f"(lambda {_s}: {BrickPrint(_s)})",
            case_flatmap=(
                lambda: f"(lambda {_a_fb}: lambda {_fa}: "
                        + f"{BrickFlatmap(_a_fb, _fa)})"
            ),
            case_pure=lambda: f"(lambda {_a}: {BrickPure(_a)})",
            case_plus=lambda: (
                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
                if typ == T_Func(T_Str, T_Func(T_Str, T_Str)) else
                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
                fail(f"Unexpected typ `{typ}` for `{s}`.")
            ),
            case_minus=lambda: (
                f"(lambda {_right}: lambda {_left}: {_left} - {_right})"
                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
                f"(lambda {_num}: - {_num})"
                if typ == T_Func(T_Bint, T_Bint) else
                fail(f"Unexpected typ `{typ}` for `{s}`.")
            ),
            case_multiply=lambda: (
                f"(lambda {_right}: lambda {_left}: {_left} * {_right})"
                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
                fail(f"Unexpected typ `{typ}` for `{s}`.")
            ),
            case_str=lambda: (
                f"(str)"
                if typ == T_Func(T_Bint, T_Str) else
                f"(str)"
                if typ == T_Func(T_Str, T_Str) else
                f"(lambda {_a}: str({_a}).lower())"
                if typ == T_Func(T_Bool, T_Str) else
                fail(f"Unexpected typ `{typ}` for `{s}`.")
            ),
            case_true=lambda: "(True)",
            case_false=lambda: "(False)",
            case_eq_eq=lambda: (
                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
                if typ == T_Func(T_Str, T_Func(T_Str, T_Bool)) else
                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bool)) else
                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
                if typ == T_Func(T_Bool, T_Func(T_Bool, T_Bool)) else
                fail(f"Unexpected typ `{typ}` for `{s}`.")
            )
        )(s)
    )
    }
     */

    def buildScalaLambda1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: List[Str], // Lists will most likely turn into dicts Map[String, _]
      lambArgStackBint: List[Bint],
      lambArgStackBool: List[Bool],
    ): BuiltScLambda =
      t_idf_x.typ match {
        case T_Unit => BuiltScLambda((_: Unit) =>
          //todo - beautify stacks
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Str => BuiltScLambda((s: Str) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, s +: lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Bint => BuiltScLambda((i: Bint) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, i +: lambArgStackBint, lambArgStackBool)
        )
        case T_Bool => BuiltScLambda((b: Bool) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, b +: lambArgStackBool)
        )

        case Typ1(`builtin_RIO`, _) => wip()
        case Typ2(`builtin_Func`, _, _) => wip()
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }

    def buildScalaWithStacks[A](
      typed: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: List[Str],
      lambArgStackBint: List[Bint],
      lambArgStackBool: List[Bool],
    ): BuiltSc =
      typed match {
        case LintedLit(s, typ) => buildScLit(s, typ)
        case LintedIdf(s, typ) => ???
        case LintedCall1(linted_f, linted_x, typ) => ???
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          buildScalaLambda1(
            linted_idf_x,
            linted_res,
            lamb_arg_stack,
            lambArgStackStr,
            lambArgStackBint,
            lambArgStackBool,
          )
      }
  }

}
