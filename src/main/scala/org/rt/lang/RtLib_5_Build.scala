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

    case class BuiltBrick(run: () => Built) extends Built
    case class BuiltLambda(f: Built => Built) extends Built





    def buildScala[A](
      typed: Linted,
    ): Built =
      Internal.buildScalaWithStacks(
        typed = typed,
        lamb_arg_stack = List.empty, // legacy todo remove
        lambArgStackStr = Map.empty,
        lambArgStackBint = Map.empty,
        lambArgStackBool = Map.empty,
      )

    def fullBuildScala(code: String) =
      buildScala(full_lint(code))
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
    def build_str_py_idf(
      s: String,
      typ: Typ,
      lamb_arg_stack: List[String],
    ): Built = {
    (
      if (lamb_arg_stack.contains(s)) // todo - should check actual stacks
        ??? //to_latin_idf(s) // todo - should likely return a value from a stack
      else
        match_builtin_idf(
            case_input = () =>
              BuiltBrick(() => BuiltStr(scala.io.StdIn.readLine())),

            case_print = () =>
              BuiltLambda {
                case BuiltStr(s) => BuiltBrick(() => BuiltUnit(println(s)))
                case _ => rtFail("runtime")
              },

            case_flatmap = () =>
              BuiltLambda(_a_fb =>
                BuiltLambda(_fa =>
                  (_a_fb, _fa) match {
                    case (BuiltLambda(a_fb), BuiltBrick(fa)) =>
                      BuiltBrick(() =>
                        a_fb(fa()) match {
                          case BuiltBrick(run) => run()
                          case _ => rtFail("runtime")
                        }
                      )
                    case _ => rtFail("runtime")
                  }
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
      lambArgStackStr: Map[String, Str],
      lambArgStackBint: Map[String, Bint],
      lambArgStackBool: Map[String, Bool],
    ): BuiltLambda =
      t_idf_x.typ match {
        case T_Unit => BuiltLambda {
          case BuiltUnit(u) =>
            //todo - beautify stacks
            buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, lambArgStackBool)
          case _ => rtFail("runtime")
        }
        case T_Str => BuiltLambda {
          case BuiltStr(s) =>
            buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr.updated(t_idf_x.s, s), lambArgStackBint, lambArgStackBool)
          case _ => rtFail("runtime")
        }
        case T_Bint => BuiltLambda {
          case BuiltBint(i) =>
            buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint.updated(t_idf_x.s, i), lambArgStackBool)
          case _ => rtFail("runtime")
        }
        case T_Bool => BuiltLambda {
          case BuiltBool(b) =>
            buildScalaWithStacks(typed_res, lamb_arg_stack, lambArgStackStr, lambArgStackBint, lambArgStackBool.updated(t_idf_x.s, b))
          case _ => rtFail("runtime")
        }

        case Typ1(`builtin_RIO`, _) => wip()
        case Typ2(`builtin_Func`, _, _) => wip()
        case _ => rtFail(s"can't build: unexpected arg type `$t_idf_x`")
      }

    def buildScalaCall1(
      typed_f: Linted,
      typed_x: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
    ): Built = {
      val nilStub = Map.empty[String, Nothing]
      val shown_f = buildScalaWithStacks(typed_f, lamb_arg_stack, nilStub, nilStub, nilStub)
      val shown_x = buildScalaWithStacks(typed_x, lamb_arg_stack, nilStub, nilStub, nilStub)

      shown_f match {
        case BuiltLambda(f) => f(shown_x)
        case _ => rtFail("runtime")
      }
    }

    def buildScalaWithStacks[A](
      typed: Linted,
      lamb_arg_stack: List[String], // legacy todo remove
      lambArgStackStr: Map[String, Str],
      lambArgStackBint: Map[String, Bint],
      lambArgStackBool: Map[String, Bool],
    ): Built =
      typed match {
        case LintedLit(s, typ) => buildScLit(s, typ)
        case LintedIdf(s, typ) => build_str_py_idf(s, typ, lamb_arg_stack)
        case LintedCall1(linted_f, linted_x, typ) => buildScalaCall1(linted_f, linted_x, lamb_arg_stack)
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

    val prototype = (
      BuiltLambda(_a_fb =>
        BuiltLambda(_fa =>
          (_a_fb, _fa) match {
            case (BuiltLambda(a_fb), BuiltBrick(fa)) =>
              BuiltBrick(() =>
                a_fb(fa()) match {
                  case BuiltBrick(run) => run()
                  case _ => rtFail("runtime")
                }
              )
            case _ => rtFail("runtime")
          }
        )
      )
        .f(
          BuiltLambda(identifier_s => (
            BuiltLambda(_a_fb =>
              BuiltLambda(_fa =>
                (_a_fb, _fa) match {
                  case (BuiltLambda(a_fb), BuiltBrick(fa)) =>
                    BuiltBrick(() =>
                      a_fb(fa()) match {
                        case BuiltBrick(run) => run()
                        case _ => rtFail("runtime")
                      }
                    )
                  case _ => rtFail("runtime")
                }
              )
            )
              .f.apply(
                BuiltLambda((identifier__u) =>
                  BuiltLambda {
                    case BuiltStr(s) => BuiltBrick(() => BuiltUnit(println(s)))
                    case _ => rtFail("runtime")
                  }.f(identifier_s)
                )
              ) match {
                case BuiltLambda(f) =>
                  println("debug 2")
                  f(
                    (BuiltLambda {
                      case BuiltStr(s) => BuiltBrick(() => BuiltUnit(println(s)))
                      case _ => rtFail("runtime")
                    }.f(identifier_s))
                  )
                case _ => rtFail("runtime")
              }
          ))
        ) match {
        case BuiltLambda(f) =>
          println("debug 1")
          f(BuiltBrick(() => BuiltStr(scala.io.StdIn.readLine())))
        case _ => rtFail("runtime")
      }
    )

    def run(
      built: Built,
    ): Unit =
      built match {
        case BuiltBrick(run) => run()
        case _ => rtFail("runtime")
      }

    println("HERE START")
    //run(prototype)
    val b = fullBuildScala(
      """s <- input
        |print(s)
        |print(s)
        |""".stripMargin
    )
    run(b)

    println("HERE FINISH")
    ()
  }
  // */
}
