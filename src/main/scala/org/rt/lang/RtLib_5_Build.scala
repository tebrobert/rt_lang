package org.rt.lang

import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.rtFail

object RtLib_5_Build {
  object Public {
    sealed trait Brick[A]

    final case object BrickInput extends Brick[String]

    final case class BrickPrint(s: String) extends Brick[Unit]

    final case class BrickPure[A](a: A) extends Brick[A]

    final case class BrickFlatmap[A, B](
      a_fb: A => Brick[B],
      fa: Brick[A],
    ) extends Brick[B]

    type Bint = Int // todo use BigInteger

    sealed trait Built

    case object BuiltUnit extends Built

    case class BuiltStr(s: String) extends Built

    case class BuiltBint(i: Bint) extends Built

    case class BuiltBool(b: Boolean) extends Built

    //case class BuiltFunc(f: Built => Built) extends Built
    case class BuiltFunc(f: Function[Built, Built]) extends Built
  }

  private object Internal {
    def buildScalaLambda_1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lamb_arg_stack: List[String],
    ): BuiltFunc = {
      val s = t_idf_x.s
      //val built = buildScala(typed_res, s +: lamb_arg_stack)
      t_idf_x.typ match {
        case T_Str => BuiltFunc((x: BuiltStr) => (??? : Built))

        //            case T_Unit => BuiltFunc((x: Unit) => buildScala(typed_res, s +: lamb_arg_stack))
        //            case T_Bint => (x: Bint) => ???
        //            case T_Bool => (x: Boolean) => ???
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }
      //f"(lambda {to_latin_idf(s)}: {built})"
    }

    def buildScala[A](
      typed: Linted,
      lamb_arg_stack: List[String] = List.empty,
    ): Built =
      typed match {
        case LintedLit(s, typ) => ???
        case LintedIdf(s, typ) => ???
        case LintedCall1(linted_f, linted_x, typ) => ???
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          ???
        //buildScalaLambda_1(linted_idf_x, linted_res, lamb_arg_stack)
      }
  }

}
