package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, wip}

object RtLib_5_Build {
  object Public {
    sealed trait Brick[A]

    case object BrickInput extends Brick[String]

    final case class BrickPrint(s: String) extends Brick[Unit]

    final case class BrickPure[A](a: A) extends Brick[A]

    final case class BrickFlatmap[A, B](
      a_fb: A => Brick[B],
      fa: Brick[A],
    ) extends Brick[B]

    type Bint = BigInt


    sealed trait Built

    sealed trait BuiltLit extends Built

    case object BuiltUnit extends BuiltLit

    case class BuiltStr(s: String) extends BuiltLit

    case class BuiltBint(i: Bint) extends BuiltLit

    case class BuiltBool(b: Boolean) extends BuiltLit

    case class BuiltFunc(
      f: (Unit => Built)
        | (String => Built)
        | (Bint => Built)
        | (Boolean => Built)
        | (BuiltFunc => Built)
    ) extends Built

    def buildScala[A](
      typed: Linted,
    ): Built =
      Internal.buildScalaWithStacks(
        typed = typed,
        lamb_arg_stack = List.empty, // legacy todo remove
        lambArgStackStr = List.empty,
        lambArgStackBint = List.empty,
      )
  }

  private object Internal {
    def buildScalaLambda_1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: List[String],
      lambArgStackBint: List[Bint],
    ): BuiltFunc =
      t_idf_x.typ match {
        case T_Unit => BuiltFunc((_: Unit) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint)
        )
//        case T_Str => BuiltFunc((s: String) => buildScalaWithStacks(typed_res, s +: lamb_arg_stack))
//        case T_Bint => BuiltFunc((i: Bint) => buildScalaWithStacks(typed_res, s +: lamb_arg_stack))
        case T_Bool => BuiltFunc((b: Boolean) => ???)
        case Typ1(`builtin_RIO`, _) => wip()
        case Typ2(`builtin_Func`, _, _) => wip()
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }

    def buildScalaWithStacks[A](
      typed: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: List[String],
      lambArgStackBint: List[Bint],
    ): Built =
      typed match {
        case LintedLit(s, typ) => ???
        case LintedIdf(s, typ) => ???
        case LintedCall1(linted_f, linted_x, typ) => ???
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          buildScalaLambda_1(
            linted_idf_x,
            linted_res,
            lamb_arg_stack,
            lambArgStackStr,
            lambArgStackBint,
          )
      }
  }

}
