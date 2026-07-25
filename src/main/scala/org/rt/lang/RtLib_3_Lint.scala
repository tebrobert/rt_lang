package org.rt.lang

import org.rt.utils.Helpers.*
import org.rt.lang.RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_1_Types.*
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Bint, T_Str, idf_to_typ, tTo}
import org.rt.lang.RtLib_2_Parse.{Expr, fullParse, match_expr}
import org.rt.utils.RtFail.*

object RtLib_3_Lint {
  sealed trait Linted {
    val typ: Typ
  }

  final case class LintedLit(
      s: String,
      typ: Typ,
  ) extends Linted

  final case class LintedIdf(
      s: String,
      typ: Typ,
  ) extends Linted

  final case class LintedCall1 private (
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
        case Unk0(_)                    => ()
        case Typ2(`builtin_Func`, _, _) => ()
        case _                          => rtFailUnsafe()
      }

      (linted_f.typ, linted_x.typ) match {
        case (Typ2(_, Unk0(_), _), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_))             => ()
        case (Typ2(_, linted_x.typ, _), x_typ) => ()
        case _                                 =>
          rtFailUnsafe(
            s"Can't create LintedCall1 with `$linted_f` and `$linted_x``",
          )
      }

      (linted_f.typ, typ) match {
        case (Typ2(_, _, Unk0(_)), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_))             => ()
        case (Typ2(_, _, `typ`), _)   => ()
        case _ => rtFailUnsafe("can't create LintedCall1")
      }

      new LintedCall1(linted_f, linted_x, typ)
    }
  }

  final case class LintedLambda1 private (
      linted_idf_x: LintedIdf,
      linted_res: Linted,
      typ: Typ,
  ) extends Linted

  object LintedLambda1 {
    def createUnlinted(
        linted_idf_x: LintedIdf,
        linted_res: Linted,
    ): LintedLambda1 =
      apply(linted_idf_x, linted_res, Unk0(-1)) // todo mb use None

    def apply(
        linted_idf_x: LintedIdf,
        linted_res: Linted,
        typ: Typ,
    ): LintedLambda1 = {
      typ match {
        case Unk0(-1)                   => () // maybe typ is always Func2
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => rtFailUnsafe("can't create LintedLambda1")
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
        case_lambda_1: (LintedIdf, Linted, Typ) => A,
    ): A =
      linted match {
        case LintedLit(s, typ)                    => case_lit(s, typ)
        case LintedIdf(s, typ)                    => case_idf(s, typ)
        case LintedCall1(linted_f, linted_x, typ) =>
          case_call_1(linted_f, linted_x, typ)
        case LintedLambda1(linted_idf_x, linted_res, typ) =>
          case_lambda_1(linted_idf_x, linted_res, typ)
      }

    def withTyp(
        new_typ: Typ,
    ): Linted =
      linted.rtMatch(
        case_lit = (s, typ) => LintedLit(s, typ),
        case_idf = (s, _) => LintedIdf(s, new_typ),
        case_call_1 =
          (typed_f, typed_x, _) => withTypCall1(typed_f, typed_x, new_typ),
        case_lambda_1 = (linted_idf_x, linted_res, _typ) =>
          withTypLambda1(linted_idf_x, linted_res, new_typ),
      )

    def hasUnk: Boolean =
      linted.rtMatch(
        case_lit = (s, typ) => typ.hasUnk,
        case_idf = (s, typ) => typ.hasUnk,
        case_call_1 = (typed_f, typed_x, typ) =>
          typ.hasUnk || typed_f.hasUnk || typed_x.hasUnk,
        case_lambda_1 = (linted_idf_x, linted_res, typ) =>
          typ.hasUnk || linted_idf_x.hasUnk || linted_res.hasUnk,
      )
  }

  private def withTypLambda1(
      linted_idf_x: LintedIdf,
      linted_res: Linted,
      new_typ: Typ,
  ): LintedLambda1 = {
    val r=
    new_typ match {
      case Unk0(_) =>
        rtFailUnsafe("not implemented...?")

      case Typ2(`builtin_Func`, new_typ_t1, new_typ_t2) =>
        val updated_linted_idf_x = LintedIdf(linted_idf_x.s, new_typ_t1)
        val updated_linted_res = linted_res.withTyp(new_typ_t2)
        LintedLambda1(
          updated_linted_idf_x,
          updated_linted_res,
          new_typ,
        )

      case _ => rtFailUnsafe(s"Unexpected type `$new_typ`.")
    }
    println(s"withTypLambda1: in linted_idf_x `$linted_idf_x` linted_res `$linted_res` new_typ `$new_typ` out `$r`")
    r
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
          val newT1 = t1
          linted_f.withTyp(t1 tTo new_typ)

        case _ => rtFailUnsafe(s"Unexpected type `${linted_f.typ}`.")
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
      case_lambda_1 =
        (_t_idf_x, typed_res, _typ) => find_idf_typ(typed_res, s_to_find),
    )

  def continue_linting_call_1_with_unknown_f(
      linted_f: Linted,
      linted_x: Linted,
  ): LintedCall1 = {
    rt_assert_type_Unk0_Unsafe(linted_f.typ)

    val new_typ_f = linted_x.typ.rtMatch(
      caseUnk0 = unk0 => unk0 tTo Unk0(unk0.i + 1),
      caseTyp0 = typ0 => typ0 tTo T_A0,
      caseTyp1 = _.mapT1(increase_unk).tTo(T_A0),
      caseTyp2 = _.mapT1(increase_unk)
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
    val linted_f_typ = rt_assert_type_Typ2Unsafe(
      linted_f.typ,
    ) // todo - try better typing
    val new_linted_x = linted_x.withTyp(linted_f_typ.t1)
    LintedCall1(linted_f, new_linted_x, linted_f_typ.t2)
  }

  def clarify(
      linted: Linted,
      clarification: Clarification,
  ): Linted =
    linted.rtMatch(
      case_lit = (_, _) => linted,
      case_idf = (s, t) => LintedIdf(s, t.clarifyUnk(clarification)),
      case_call_1 = (linF, linX, typ) =>
        LintedCall1(
          clarify(linF, clarification),
          clarify(linX, clarification),
          typ.clarifyUnk(clarification),
        ),
      case_lambda_1 = (linArg, linRes, typ) =>
        LintedLambda1(
          LintedIdf(linArg.s, linArg.typ.clarifyUnk(clarification)),
          clarify(linRes, clarification),
          typ.clarifyUnk(clarification),
        ),
    )

  def continue_linting_call_1(
      linted_f: Linted,
      linted_x: Linted,
  ): LintedCall1 = {
    val (new_typ_f, clarifications) =
      linted_f.typ.concretizeAsFunc(linted_x.typ)

    val new_linted_f2 =
      clarifications.foldLeft(linted_f) { (l, c) =>
        clarify(l, c)
      }

    val new_typ2_f = rt_assert_type_Typ2Unsafe(
      new_typ_f,
    ) // todo - try better typing
    val new_linted_f = linted_f.withTyp(new_typ2_f)
    val new_linted_x = linted_x.withTyp(new_typ2_f.t1)
    val r=
    LintedCall1(new_linted_f2, new_linted_x, new_typ2_f.t2)
    println(s"continue_linting_call_1: in linted_f `$linted_f` linted_x `$linted_x` out `$r`")
    r
  }

  def lint_set_call_1(
      expr_f: Expr,
      expr_x: Expr,
  ) =
    (for {
      linted_f <- lint_set(expr_f)
      _ = rtAssertUnsafe(
        (linted_f.typ.isInstanceOf[Typ2]
          && linted_f.typ
            .asInstanceOf[Typ2]
            .s == builtin_Func // todo - try better typing
        ) || linted_f.typ.isInstanceOf[Unk0],
      )
      linted_x <- lint_set(expr_x)

      mb_current_linted_call1 = rt_try(() =>
        if (linted_f.typ.isInstanceOf[Unk0])
          continue_linting_call_1_with_unknown_f(linted_f, linted_x)
        else if (linted_x.typ.isInstanceOf[Unk0])
          continue_linting_call_1_with_unknown_x(linted_f, linted_x)
        else continue_linting_call_1(linted_f, linted_x),
      )
    } yield mb_current_linted_call1)
      .collect { case Right(linted) => linted }

  // todo - shorten
  def lint_set_lambda_1(
      expr_arg: Expr,
      expr_res: Expr,
  ) =
    (for {
      linted_arg_raw <- lint_set(expr_arg)
      linted_arg = rt_assert_type_LintedIdf_Unsafe(
        linted_arg_raw,
      ) // todo - try better typing
      linted_res <- lint_set(expr_res)

      mb_res = rt_try { () =>
        val linted_arg_s =
          linted_arg.rtMatch(
            case_idf = (s, _) => s,
            case_lit = (_, _) => rtFailUnsafe(),
            case_call_1 = (_, _, _) => rtFailUnsafe(),
            case_lambda_1 = (_, _, _) => rtFailUnsafe(),
          ) // todo - try better typing

        val found_typ_arg = find_idf_typ(linted_res, linted_arg_s)
        val relinted_arg = LintedIdf(linted_arg.s, found_typ_arg)

        // todo - likely, next Unk0 needed
        LintedLambda1.createUnlinted(relinted_arg, linted_res)
      }
    } yield mb_res)
      .collect { case Right(linted) => linted }

  def lint_set_idf(s: String) =
    idf_to_typ
      .getOrElse(s, Set(T_A0))
      .map(typ => LintedIdf(s, typ))

  def lint_set(expr: Expr): Set[Linted] =
  {
    val r =
    match_expr(
      case_lit_str = s => Iterable(LintedLit(s, T_Str)),
      case_lit_bint = i => Iterable(LintedLit(i, T_Bint)),
      case_idf = lint_set_idf,
      case_call_1 = lint_set_call_1,
      case_lambda_1 = lint_set_lambda_1,
      case_braced = lint_set,
    )(expr).toSet

    println(s"[[lint_set: in `$expr` out `$r`]]")
    r
  }

  def lint(
      expr: Expr,
  ): Either[RtFail, Linted] = {
    val linted_set = lint_set(expr/*, lintedIdentifiers = Map.empty*/) //todo
    linted_set.toList match {
      case head :: Nil if !head.hasUnk =>
        Right(head)
      case _ => rtFail(s"Can't lint `$expr` with `$linted_set`")
    }
  }

  def fullLint(code: String): Either[RtFail, Linted] =
    fullParse(code).flatMap(lint)
}
