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

    sealed trait Built
    case class BuiltUnit(u: Unit) extends Built
    case class BuiltStr(s: Str) extends Built
    case class BuiltBint(i: Bint) extends Built
    case class BuiltBool(b: Bool) extends Built

    sealed trait BuiltLambda extends Built
    case class BuiltLambdaA[A](f: A => Built) extends BuiltLambda
    case class BuiltLambdaABrick[A](f: A => BuiltBrick) extends BuiltLambda
//    case class BuiltLambdaUnit(f: Unit => Built) extends BuiltLambda
//    case class BuiltLambdaStr(f: Str => Built) extends BuiltLambda
//    case class BuiltLambdaBint(f: Bint => Built) extends BuiltLambda
//    case class BuiltLambdaBool(f: Bool => Built) extends BuiltLambda
//    case class BuiltLambdaBrick[A](f: Brick[A] => Built) extends BuiltLambda
//    case class BuiltLambdaLambda(f: BuiltLambda => Built) extends BuiltLambda

//    sealed trait BuiltBrick[A] extends Built
//    case object BuiltBrickInput extends BuiltBrick[Str]
//    final case class BuiltBrickPrint(s: Str) extends BuiltBrick[Unit]
//    final case class BuiltBrickPure[A](a: A) extends BuiltBrick[A]
//    final case class BuiltBrickFlatmap[A, B](
//      a_fb: A => BuiltBrick[B],
//      fa: BuiltBrick[A],
//    ) extends BuiltBrick[B]

    case class BuiltBrick(run: () => Built) extends Built




    def buildScala[A](
      typed: Linted,
    ): Built =
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
    def build_str_py_idf[A](
      s: String,
      typ: Typ,
      lamb_arg_stack: List[String],
    ): Built = {
    (
      if (lamb_arg_stack.contains(s)) // todo - should check actual stacks
        ??? //to_latin_idf(s) // todo - should likely return a value from a stack
      else
        match_builtin_idf(
            case_input = () => BuiltBrick(() => BuiltStr(scala.io.StdIn.readLine())),
            case_print = () => BuiltLambdaA((_s: Built) =>
              _s match {
                case BuiltStr(s) => BuiltBrick(() => BuiltUnit(println(s)))
                case _ => rtFail("runTime") //todo - compile errors are much better
              }
            ),
            case_flatmap = () =>
              BuiltLambdaA((_a_fb: BuiltLambdaABrick[Built]) =>
                  BuiltLambdaABrick((_fa: BuiltBrick) =>
                      BuiltBrick(() => _a_fb.f(_fa.run()).run())
                  )
              ),
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
    ): BuiltLambda =
      t_idf_x.typ match {
        case T_Unit => BuiltLambdaA((_: Unit) =>
          //todo - beautify stacks
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Str => BuiltLambdaA((s: Str) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, s +: lambArgStackStr, lambArgStackBint, lambArgStackBool)
        )
        case T_Bint => BuiltLambdaA((i: Bint) =>
          buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, i +: lambArgStackBint, lambArgStackBool)
        )
        case T_Bool => BuiltLambdaA((b: Bool) =>
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
    ): Built =
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

  //*
  val test8 = {
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
      BuiltLambdaA((_a_fb: BuiltLambdaABrick[Built]) =>
        BuiltLambdaABrick((_fa: BuiltBrick) =>
          BuiltBrick(() => _a_fb.f(_fa.run()).run())
        )
      ).f
      (
        BuiltLambdaABrick[Built]((identifier_s: Built) => (
          (
            BuiltLambdaA((_a_fb: BuiltLambdaABrick[Built]) =>
              BuiltLambdaABrick((_fa: BuiltBrick) =>
                BuiltBrick(() => _a_fb.f(_fa.run()).run())
              )
            )
          ).f
          (
            BuiltLambdaABrick[Unit]((identifier__u: Unit) =>
              BuiltLambdaA((_s: Str) => BuiltBrick(() => BuiltUnit(println(_s)))).f(identifier_s.s)
            )
          ).f
          (BuiltLambdaA((_s: Str) => BuiltBrick(() => BuiltUnit(println(_s)))).f(identifier_s))
        ))
      )//.f
      //(BuiltBrick(() => BuiltStr(scala.io.StdIn.readLine())))
    )

    println("HERE START")
    x.run()
    println("HERE FINISH")
    ()
  }
  // */
}
