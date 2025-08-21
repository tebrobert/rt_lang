package org.rt.lang

import org.rt.Helpers.*
import org.rt.lang.RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Bint, T_Str, idf_to_typ, tTo}
import org.rt.lang.RtLib_3_Parse.{Expr, full_parse, match_expr}
import org.rt.utils.RtFail.*

object RtLib_4_Lint {
  sealed trait Linted {
    val typ: Typ
  }

  private final case class LintedLit(
    s: String,
    typ: Typ,
  ) extends Linted

  final case class LintedIdf(
    s: String,
    typ: Typ,
  ) extends Linted

  final case class LintedCall1 private(
    linted_f: Linted,
    linted_x: Linted,
    typ: Typ,
  ) extends Linted

  object LintedCall1 {
    def apply(
      linted_f: Linted,
      linted_x: Linted,
      typ: Typ,
    ): LintedCall1 = {
      linted_f.typ match {
        case Unk0(_) => ()
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => rtFail()
      }

      (linted_f.typ, linted_x.typ) match {
        case (Typ2(_, Unk0(_), _), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_)) => ()
        case (Typ2(_, linted_x.typ, _), x_typ) => ()
        case _ => rtFail("can't create LintedCall1")
      }

      (linted_f.typ, typ) match {
        case (Typ2(_, _, Unk0(_)), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_)) => ()
        case (Typ2(_, _, `typ`), _) => ()
        case _ => rtFail("can't create LintedCall1")
      }

      new LintedCall1(linted_f, linted_x, typ)
    }
  }

  final case class LintedLambda1 private(
    linted_idf_x: Linted,
    linted_res: Linted,
    typ: Typ,
  ) extends Linted

  object LintedLambda1 {
    def createUnlinted(
      linted_idf_x: Linted,
      linted_res: Linted,
    ): LintedLambda1 =
      apply(linted_idf_x, linted_res, Unk0(-1)) // todo mb use None

    def apply(
      linted_idf_x: Linted,
      linted_res: Linted,
      typ: Typ,
    ): LintedLambda1 = {
      linted_idf_x match {
        case LintedIdf(_, _) => () // perhaps: specify arg type
        case _ => rtFail("can't create LintedLambda1")
      }

      typ match {
        case Unk0(-1) => () // maybe typ is always Func2
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => rtFail("can't create LintedLambda1")
      }

      new LintedLambda1(
        linted_idf_x,
        linted_res,
        if (typ != Unk0(-1)) // perhaps: intermediatory Unk0(-1) unneeded
          typ
        else linted_idf_x.typ tTo linted_res.typ,
      )
    }
  }

  extension (linted: Linted) {
    def rtMatch[A](
      case_lit: (String, Typ) => A,
      case_idf: (String, Typ) => A,
      case_call_1: (Linted, Linted, Typ) => A,
      case_lambda_1: (Linted, Linted, Typ) => A,
    ): A =
      linted match {
        case LintedLit(s, typ) => case_lit(s, typ)
        case LintedIdf(s, typ) => case_idf(s, typ)
        case LintedCall1(linted_f, linted_x, typ) =>
          case_call_1(linted_f, linted_x, typ)
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          case_lambda_1(linted_idf_x, linted_res, typ)
      }

    def withTyp(
      new_typ: Typ
    ): Linted =
      linted.rtMatch(
        case_lit = (s, typ) => LintedLit(s, typ),
        case_idf = (s, _) => LintedIdf(s, new_typ),
        case_call_1 = (typed_f, typed_x, _) =>
          withTypCall1(typed_f, typed_x, new_typ),
        case_lambda_1 = (linted_idf_x, linted_res, _typ) =>
          withTypLambda1(linted_idf_x, linted_res, new_typ),
      )
  }

  private def withTypLambda1(
    linted_idf_x: Linted,
    linted_res: Linted,
    new_typ: Typ,
  ): LintedLambda1 = {
    new_typ match {
      case Unk0(_) =>
        rtFail("not implemented...?")

      case Typ2(`builtin_Func`, new_typ_t1, new_typ_t2) =>
        val updated_linted_idf_x = linted_idf_x.withTyp(new_typ_t1)
        val updated_linted_res = linted_res.withTyp(new_typ_t2)
        LintedLambda1(
          updated_linted_idf_x,
          updated_linted_res,
          new_typ,
        )

      case _ => rtFail(s"Unexpected type `$new_typ`.")
    }
  }

  private def withTypCall1(
    linted_f: Linted,
    linted_x: Linted,
    new_typ: Typ,
  ): Linted =
    LintedCall1(
      linted_f.typ match {
        case Unk0(i) =>
          linted_f.withTyp(linted_x.typ tTo new_typ) // typed_f - legacy comment

        case Typ2(_, t1, _) =>
          linted_f.withTyp(t1 tTo new_typ)
            .tapDebug(res => println(
              s"""withTypCall1(
                 |  linted_f = $linted_f
                 |  linted_x = $linted_x
                 |  new_typ = $new_typ
                 |) {
                 |   t1 = $t1
                 |   res = $res
                 |}""".stripMargin
            ))

        case _ => rtFail(s"Unexpected type `${linted_f.typ}`.")
      },
      linted_x,
      new_typ,
    )

  def find_idf_typ_call_1(
    linted_f: Linted,
    linted_x: Linted,
    s_to_find: String,
  ) = {
    val lookup_by_f = find_idf_typ(linted_f, s_to_find)

    if (!lookup_by_f.isInstanceOf[Unk0])
      lookup_by_f
    else find_idf_typ(linted_x, s_to_find)
  }

  def find_idf_typ(
    linted: Linted,
    s_to_find: String,
  ): Typ =
    linted.rtMatch(
      case_lit = (_s, _typ) => T_A0,
      case_idf = (s, typ) => if (s == s_to_find) typ else T_A0,
      case_call_1 = (typed_f, typed_x, _typ) =>
        find_idf_typ_call_1(typed_f, typed_x, s_to_find),
      case_lambda_1 = (_t_idf_x, typed_res, _typ) =>
        find_idf_typ(typed_res, s_to_find),
    )

  def continue_linting_call_1_with_unknown_f(
    linted_f: Linted,
    linted_x: Linted,
  ): LintedCall1 = {
    rt_assert_type_Unk0(linted_f.typ)

    val new_typ_f = linted_x.typ.rtMatch(
      caseUnk0 = unk0 => unk0 tTo Unk0(unk0.i + 1),
      caseTyp0 = typ0 => typ0 tTo T_A0,
      caseTyp1 = _.mapT1(increase_unk).tTo(T_A0),
      caseTyp2 = _
        .mapT1(increase_unk)
        .mapT2(increase_unk)
        .tTo(T_A0),
    )

    val new_linted_f = linted_f.withTyp(new_typ_f)
    LintedCall1(new_linted_f, linted_x, new_typ_f.t2)
  }

  def continue_linting_call_1_with_unknown_x(
    linted_f: Linted,
    linted_x: Linted,
  ): LintedCall1 = {
    val linted_f_typ = rt_assert_type_Typ2(linted_f.typ) // todo - try better typing
    val new_linted_x = linted_x.withTyp(linted_f_typ.t1)
    LintedCall1(linted_f, new_linted_x, linted_f_typ.t2)
  }

  def continue_linting_call_1(
    linted_f: Linted,
    linted_x: Linted,
  ): LintedCall1 = {
    val new_typ_f = linted_f.typ.concretizeAsFunc(linted_x.typ)
    //Typ2(Func,Typ1(RIO,Typ0(Unit)),Typ1(RIO,Typ0(Unit)))

    val new_typ2_f = rt_assert_type_Typ2(new_typ_f) // todo - try better typing
    val new_linted_f = linted_f.withTyp(new_typ2_f)
    val new_linted_x = linted_x.withTyp(new_typ2_f.t1)
    LintedCall1(new_linted_f, new_linted_x, new_typ2_f.t2)
      .tapDebug(res => println(
        s"""continue_linting_call_1:
           |    linted_f = $linted_f
           |    new_typ2_f = $new_typ2_f
           |    new_linted_f = $new_linted_f
           |""".stripMargin
      ))
  }

  def lint_set_call_1(
    expr_f: Expr,
    expr_x: Expr,
  ) =
    (for {
      linted_f <- lint_set(expr_f)
      _ = rt_assert(
        (linted_f.typ.isInstanceOf[Typ2]
          && linted_f.typ.asInstanceOf[Typ2].s == builtin_Func // todo - try better typing
          ) || linted_f.typ.isInstanceOf[Unk0]
      )
      linted_x <- lint_set(expr_x)

      mb_current_linted_call1 = rt_try(() =>
        if (linted_f.typ.isInstanceOf[Unk0])
          continue_linting_call_1_with_unknown_f(linted_f, linted_x)
        else if (linted_x.typ.isInstanceOf[Unk0])
          continue_linting_call_1_with_unknown_x(linted_f, linted_x)
        else continue_linting_call_1(linted_f, linted_x)
      )
    } yield mb_current_linted_call1)
      .collect { case Right(linted) => linted }

  def lint_set_lambda_1(
    expr_arg: Expr,
    expr_res: Expr,
  ) =
    (for {
      linted_arg <- lint_set(expr_arg)
      linted_res <- lint_set(expr_res)

      mb_res = rt_try { () =>
        val linted_arg_s =
          linted_arg.rtMatch(
            case_idf = (s, _) => s,
            case_lit = (_, _) => rtFail(),
            case_call_1 = (_, _, _) => rtFail(),
            case_lambda_1 = (_, _, _) => rtFail(),
          ) // todo - try better typing

        val found_typ_arg = find_idf_typ(linted_res, linted_arg_s)
        val relinted_arg = linted_arg.withTyp(found_typ_arg)

        //todo - likely, next Unk0 needed
        LintedLambda1.createUnlinted(relinted_arg, linted_res)
      }
    } yield mb_res)
      .collect { case Right(linted) => linted }

  def lint_set_idf(s: String) =
    idf_to_typ.getOrElse(s, Set(T_A0))
      .map(typ => LintedIdf(s, typ))

  def lint_set(expr: Expr): Set[Linted] =
    match_expr(
      case_lit_str = s => Iterable(LintedLit(s, T_Str)),
      case_lit_bint = i => Iterable(LintedLit(i, T_Bint)),
      case_idf = lint_set_idf,
      case_call_1 = lint_set_call_1,
      case_lambda_1 = lint_set_lambda_1,
      case_braced = lint_set,
    )(expr).toSet

  def lint(
    expr: Expr,
  ): Linted = {
    val linted_set = lint_set(expr)
    linted_set.toList match {
      case head :: Nil => head
      case _ => rtFail(linted_set.toString)
    }
  }

  def full_lint(code: String) =
    lint(full_parse(code))
}
