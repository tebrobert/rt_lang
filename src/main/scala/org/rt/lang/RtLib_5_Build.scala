package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.*
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.lang.RtLib_4_Lint.{Linted, LintedCall1, LintedIdf, LintedLambda1, LintedLit}
import org.rt.lang.RtLib_5_Build.Public.*
import org.rt.utils.RtFail.{rtFail, rt_try, wip}

object RtLib_5_Build {
  object Public {

    type Str = String
    type Bint = BigInt
    type Bool = Boolean

    sealed trait Wrapped
    sealed trait WrappedLit extends Wrapped // Sc - Scala
    case object WrappedLitUnit extends WrappedLit
    case class WrappedLitStr(s: Str) extends WrappedLit
    case class WrappedLitBint(i: Bint) extends WrappedLit
    case class WrappedLitBool(b: Bool) extends WrappedLit
    case class WrappedLambdaLegacy(
      f: (Unit => Wrapped)
        | (Str => Wrapped)
        | (Bint => Wrapped)
        | (Bool => Wrapped)
        // todo Brick?
        | (WrappedLambdaLegacy => Wrapped)
    ) extends Wrapped

    sealed trait WrappedLambda extends Wrapped
    case class WrappedLambdaA[A](f: A => Wrapped) extends WrappedLambda
//    case class WrappedLambdaUnit(f: Unit => Wrapped) extends WrappedLambda
//    case class WrappedLambdaStr(f: Str => Wrapped) extends WrappedLambda
//    case class WrappedLambdaBint(f: Bint => Wrapped) extends WrappedLambda
//    case class WrappedLambdaBool(f: Bool => Wrapped) extends WrappedLambda
//    case class WrappedLambdaBrick[A](f: Brick[A] => Wrapped) extends WrappedLambda
//    case class WrappedLambdaLambda(f: WrappedLambda => Wrapped) extends WrappedLambda

    sealed trait Brick[A] extends Wrapped
    case object BrickInput extends Brick[Str]
    final case class BrickPrint(s: Str) extends Brick[Unit]
    final case class BrickPure[A](a: A) extends Brick[A]
    final case class BrickFlatmap[A, B](
      a_fb: A => Brick[B],
      fa: Brick[A],
    ) extends Brick[B]
    //case class BrickLambda[A, B](f: A => Brick[B]) extends Brick[B] //todo wth



    def buildScala[A](
      typed: Linted,
    ): Wrapped =
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

      WrappedLitBint(bint)
    }


    def buildScLit(
      serializedValue: String,
      typ: Typ,
    ): WrappedLit = {
      if (typ == T_Str)
        WrappedLitStr(serializedValue)
      else if (typ == T_Bint)
        buildScLitBint(serializedValue)
      else rtFail(s"Unexpected literal: `$serializedValue`, `$typ`.")
    }

    def to_latin_idf: String => String = ???

    // todo - shorten
    def build_str_py_idf[A](
      s: String,
      typ: Typ,
      lamb_arg_stack: List[String],
    ): Wrapped = {
    (
      if (lamb_arg_stack.contains(s)) // todo - should check actual stacks
        ??? //to_latin_idf(s) // todo - should likely return a value from a stack
      else
        match_builtin_idf(
            case_input=() => BrickInput,
            case_print=() => WrappedLambdaA(_s => BrickPrint(_s)),
            case_flatmap=() => ???,
              //()=> WrappedLambdaA(_a_fb => WrappedLambdaA(_fa => BrickFlatmap(_a_fb, _fa))), // todo omggggggg
          case_pure = () => ???,
          case_plus = () => ???,
//            case_pure=lambda: f"(lambda {_a}: {BrickPure(_a)})",
//            case_plus=lambda: (
//                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
//                if typ == T_Func(T_Str, T_Func(T_Str, T_Str)) else
//                f"(lambda {_right}: lambda {_left}: {_left} + {_right})"
//                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
//                fail(f"Unexpected typ `{typ}` for `{s}`.")
//            ),
          case_minus = () => ???,
//            case_minus=lambda: (
//                f"(lambda {_right}: lambda {_left}: {_left} - {_right})"
//                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
//                f"(lambda {_num}: - {_num})"
//                if typ == T_Func(T_Bint, T_Bint) else
//                fail(f"Unexpected typ `{typ}` for `{s}`.")
//            ),
          case_multiply = () => ???,
//            case_multiply=lambda: (
//                f"(lambda {_right}: lambda {_left}: {_left} * {_right})"
//                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bint)) else
//                fail(f"Unexpected typ `{typ}` for `{s}`.")
//            ),
          case_str = () => ???,
//            case_str=lambda: (
//                f"(str)"
//                if typ == T_Func(T_Bint, T_Str) else
//                f"(str)"
//                if typ == T_Func(T_Str, T_Str) else
//                f"(lambda {_a}: str({_a}).lower())"
//                if typ == T_Func(T_Bool, T_Str) else
//                fail(f"Unexpected typ `{typ}` for `{s}`.")
//            ),
          case_true = () => ???,
//            case_true=lambda: "(True)",
          case_false = () => ???,
//            case_false=lambda: "(False)",
          case_eq_eq = () => ???,
//            case_eq_eq=lambda: (
//                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
//                if typ == T_Func(T_Str, T_Func(T_Str, T_Bool)) else
//                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
//                if typ == T_Func(T_Bint, T_Func(T_Bint, T_Bool)) else
//                f"(lambda {_right}: lambda {_left}: {_left} == {_right})"
//                if typ == T_Func(T_Bool, T_Func(T_Bool, T_Bool)) else
//                fail(f"Unexpected typ `{typ}` for `{s}`.")
//            )
        )(s)
    )
    }

    def buildScalaLambda1(
      t_idf_x: LintedIdf,
      typed_res: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: List[Str], // Lists will most likely turn into dicts Map[String, _]
      lambArgStackBint: List[Bint],
      lambArgStackBool: List[Bool],
    ): WrappedLambda =
      t_idf_x.typ match {
        case T_Unit => WrappedLambdaA((_: Unit) =>
          //todo - beautify stacks
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Str => WrappedLambdaA((s: Str) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, s +: lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Bint => WrappedLambdaA((i: Bint) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, i +: lambArgStackBint, lambArgStackBool)
        )
        case T_Bool => WrappedLambdaA((b: Bool) =>
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
    ): Wrapped =
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

  /*
  private val test8 = {
          // s <- input
          // print(s)
          // print(s)
//    (
//      (lambda identifier_a_fb: lambda identifier_fa: BrickFlatmap(identifier_a_fb, identifier_fa))
//      (
//        (
//          lambda identifier_s: (
//            (lambda identifier_a_fb: lambda identifier_fa: BrickFlatmap(identifier_a_fb, identifier_fa))
//            (lambda identifier__: (lambda identifier_s: BrickPrint(identifier_s))(identifier_s))
//          )((lambda identifier_s: BrickPrint(identifier_s))(identifier_s))
//        )
//      )
//    )
//    (BrickInput())

    val x = (
      ((identifier_a_fb: Str=>Brick[Unit]) => (identifier_fa: Brick[Str]) => BrickFlatmap(identifier_a_fb, identifier_fa))
      (
        (identifier_s: Str) => (
          ((identifier_a_fb: Unit => Brick[Unit]) => (identifier_fa: Brick[Unit]) => BrickFlatmap(identifier_a_fb, identifier_fa))
          ((identifier__u: Unit) => ((identifier_s: Str) => BrickPrint(identifier_s))(identifier_s))
        )(((identifier_s: Str) => BrickPrint(identifier_s))(identifier_s))
      )
      (BrickInput)
    )

    def unsafe_run_built(rio: Brick[Any]): Any =
      (rio match {
        case BrickInput => () => scala.io.StdIn.readLine()
        case BrickPrint(s) => () => println(s)
        case BrickFlatmap(a_fb, fa) => () => unsafe_run_built(a_fb(unsafe_run_built(fa)))
        case BrickPure(a) => () => a
      })()

    unsafe_run_built(x)
  }
  // */
}
