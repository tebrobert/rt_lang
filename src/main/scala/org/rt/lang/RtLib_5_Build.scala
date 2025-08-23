package org.rt.lang

import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedIdf}
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
  }

  private object Internal {
    def build_str_py_lambda_1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lamb_arg_stack: List[String],
    ): Nothing => Nothing = {
      val s = t_idf_x.s
      val built = build_str_py(typed_res, s +: lamb_arg_stack)
      t_idf_x.typ match {
        case T_Unit => (_: Unit) => ???
        case T_Str => (x: String) => ???
        case T_Bint => (x: Int) => ??? // todo use BigInteger
        case T_Bool => (x: Boolean) => ???
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }
      //f"(lambda {to_latin_idf(s)}: {built})"
    }

    def build_str_py(
      typed: Linted,
      lamb_arg_stack: List[String] = List.empty,
    ): Brick[Unit] =
      ???
  }

}
