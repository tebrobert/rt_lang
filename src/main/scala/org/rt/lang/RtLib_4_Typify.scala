package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_1_Types.{Typ, Typ0, Typ1, Typ2, Unk0, increase_unk, match_typ, update_typ}
import org.rt.lang.RtLib_0_2_Builtins.{T_A0, T_Bint, T_Func, T_Str, idf_to_typ}
import org.rt.lang.RtLib_3_Parse.{Expr, full_parse, match_expr}
import org.rt.utils.RtFail.{rtFail, rt_assert, rt_assert_equal, rt_assert_type_Typ2, rt_assert_type_Unk0, rt_try, wip}

object RtLib_4_Typify {
  sealed trait Typified {
    val typ: Typ
  }

  private final case class TypifiedLit(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedIdf(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedCall1 private(
    typified_f: Typified,
    typified_x: Typified,
    typ: Typ,
  ) extends Typified

  object TypifiedCall1 {
    def apply(typified_f: Typified, typified_x: Typified, typ: Typ): TypifiedCall1 = {
      typified_f.typ match {
        case Unk0(_) => ()
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => rtFail()
      }

      (typified_f.typ, typified_x.typ) match {
        case (Typ2(_, Unk0(_), _), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_)) => ()
        case (Typ2(_, typified_x.typ, _), x_typ) => ()
        case _ => rtFail()
      }

      (typified_f.typ, typ) match {
        case (Typ2(_, _, Unk0(_)), _) => () // suspicious: what if typ_f is Unk
        case (_, Unk0(_)) => ()
        case (Typ2(_, _, `typ`), _) => ()
        case _ => rtFail()
      }

      new TypifiedCall1(typified_f, typified_x, typ)
    }
  }

  final case class TypifiedLambda1 private(
    typified_idf_x: Typified,
    typified_res: Typified,
    typ: Typ,
  ) extends Typified

  object TypifiedLambda1 {
    def apply(typified_idf_x: Typified, typified_res: Typified, typ: Typ): TypifiedLambda1 = {
      typified_idf_x match {
        case TypifiedIdf(_, _) => () // perhaps: specify arg type
        case _ => rtFail()
      }

      typ match {
        case Unk0(-1) => () // maybe typ is always Func2
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => rtFail()
      }

      new TypifiedLambda1(
        typified_idf_x,
        typified_res,
        if (typ != Unk0(-1)) // perhaps: intermediatory Unk0(-1) unneeded
          typ
        else T_Func(typified_idf_x.typ, typified_res.typ),
      )
    }
  }

  private def match_typified[A](
    case_lit: (String, Typ) => A,
    case_idf: (String, Typ) => A,
    case_call_1: (Typified, Typified, Typ) => A,
    case_lambda_1: (Typified, Typified, Typ) => A,
  ): Typified => A = {
    case TypifiedLit(s, typ) => case_lit(s, typ)
    case TypifiedIdf(s, typ) => case_idf(s, typ)
    case TypifiedCall1(typified_f, typified_x, typ) => case_call_1(typified_f, typified_x, typ)
    case TypifiedLambda1(typified_idf_x, typified_res, typ) => case_lambda_1(typified_idf_x, typified_res, typ)
  }

  private def replace_typ_lambda_1(typified_idf_x: Typified, typified_res: Typified, new_typ: Typ): TypifiedLambda1 = {
    new_typ match {
      case Unk0(_) =>
        rtFail("not implemented...?")

      case Typ2(`builtin_Func`, new_typ_t1, new_typ_t2) =>
        val updated_typified_idf_x = replace_typ(typified_idf_x, new_typ_t1)
        val updated_typified_res = replace_typ(typified_res, new_typ_t2)
        TypifiedLambda1(
          updated_typified_idf_x,
          updated_typified_res,
          new_typ,
        )

      case _ => rtFail(s"Unexpected type `$new_typ`.")
    }
  }

  private def replace_typ_call_1(typed_f: Typified, typed_x: Typified, new_typ: Typ): Typified =
    TypifiedCall1(
      typed_f.typ match {
        case Unk0(i) =>
          replace_typ(typed_f, T_Func(typed_x.typ, new_typ)) // typed_f - legacy comment

        case Typ2(_, t1, _) =>
          replace_typ(typed_f, T_Func(t1, new_typ))

        case _ => rtFail(s"Unexpected type `${typed_f.typ}`.")
      },
      typed_x,
      new_typ,
    )

  private def replace_typ(typified: Typified, new_typ: Typ): Typified =
    match_typified(
      case_lit = (s, typ) => TypifiedLit(s, typ),
      case_idf = (s, _) => TypifiedIdf(s, new_typ),
      case_call_1 = (typed_f, typed_x, _) => replace_typ_call_1(typed_f, typed_x, new_typ),
      case_lambda_1 = (typified_idf_x, typified_res, _typ) =>
        replace_typ_lambda_1(typified_idf_x, typified_res, new_typ),
    )(typified)


  def get_unknowns_fot_typ(typ: Typ): Set[String] =
    match_typ(
      case_typ0 = _s => Set.empty[String],
      case_unk0 = s => Set(s.toString), // todo - try a more proper type
      case_typ1 = (_s, t1) => get_unknowns_fot_typ(t1),
      case_typ2 = (_s, t1, t2) =>
        get_unknowns_fot_typ(t1) ++ get_unknowns_fot_typ(t2),
    )(typ)

  def get_unknowns_for_typified(typified: Typified) =
    match_typified(
      case_lit = (_s, typ) => get_unknowns_fot_typ(typ),
      case_idf = (_s, typ) => get_unknowns_fot_typ(typ),
      case_call_1 = (_typed_f, _typed_x, typ) => get_unknowns_fot_typ(typ),
      case_lambda_1 = (_typified_idf_x, _typified_res, typ) =>
        get_unknowns_fot_typ(typ),
    )(typified)

  def find_idf_typ_call_1(
    typified_f: Typified,
    typified_x: Typified,
    s_to_find: String,
  ) = {
    val lookup_by_f = find_idf_typ(typified_f, s_to_find)

    if (!lookup_by_f.isInstanceOf[Unk0])
      lookup_by_f
    else find_idf_typ(typified_x, s_to_find)
  }

  def find_idf_typ(typified: Typified, s_to_find: String): Typ =
    match_typified(
      case_lit = (_s, _typ) => T_A0,
      case_idf = (s, typ) => if (s == s_to_find) typ else T_A0,
      case_call_1 = (typed_f, typed_x, _typ) =>
        find_idf_typ_call_1(typed_f, typed_x, s_to_find),
      case_lambda_1 = (_t_idf_x, typed_res, _typ) =>
        find_idf_typ(typed_res, s_to_find),
    )(typified)

  // hmmm
  def concrete_f_unk0(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_x: Typ,
    sub_fx_i: Int,
  ) = {
    val case_known =
      () => concrete_f(
        update_typ(Unk0(sub_fx_i), typ_sub_x)(typ_f),
        typ_x,
      )

    match_typ(
      case_unk0 = i => if (i == sub_fx_i) typ_f else wip(),
      case_typ0 = _s => case_known(),
      case_typ1 = (_s, _t1) => case_known(),
      case_typ2 = (_s, _t1, _t2) => case_known(),
    )(typ_sub_x)
  }

  def concrete_f_typ0(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_x: Typ,
    sub_fx_s: String
  ) =
    match_typ(
      case_unk0 = i => concrete_f(
        update_typ(Unk0(i), Typ0(sub_fx_s))(typ_f),
        update_typ(Unk0(i), Typ0(sub_fx_s))(typ_x),
      ),
      case_typ0 = s => if (s == sub_fx_s) typ_f else rtFail(),
      case_typ1 = (_s, _t1) => rtFail(),
      case_typ2 = (_s, _t1, _t2) => rtFail(),
    )(typ_sub_x)

  def concrete_f_typ1(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_x: Typ,
    sub_fx_s: String,
    sub_fx_t1: Typ,
  ) =
    match_typ(
      case_unk0 = i => typ_f,
      case_typ0 = s => rtFail(),
      case_typ1 = (s, t1) => if (s == sub_fx_s) concrete_f_rec(typ_f, typ_x,
        sub_fx_t1, t1,
      ) else rtFail(),
      case_typ2 = (s, t1, t2) => rtFail(),
    )(typ_sub_x)


  def concrete_f_typ2_typ2(
    typ_f: Typ,
    typ_x: Typ,
    sub_x_s: String,
    sub_x_t1: Typ,
    sub_x_t2: Typ,
  )(
    sub_fx_s: String,
    sub_fx_t1: Typ,
    sub_fx_t2: Typ,
  ) = {
    rt_assert_equal(sub_x_s, sub_fx_s)
    val used_t1 = concrete_f_rec(typ_f, typ_x, sub_fx_t1, sub_x_t1)
    val (f1, x1) = (used_t1, rt_assert_type_Typ2(used_t1).t1) // todo - try better typing
    val used_t2 = concrete_f_rec(f1, x1, sub_fx_t2, sub_x_t2)
    used_t2
  }

  def concrete_f_typ2(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_x: Typ,
    sub_fx_s: String,
    sub_fx_t1: Typ,
    sub_fx_t2: Typ,
  ) = {
    val bad_type =
      () => rtFail(s"Can't match the types $sub_fx_s vs $typ_sub_x")

    match_typ(
      case_unk0 = _i => wip(),
      case_typ0 = _s => bad_type(),
      case_typ1 = (_s, _t1) => bad_type(),
      case_typ2 = (sub_x_s, sub_x_t1, sub_x_t2) => concrete_f_typ2_typ2(
        typ_f, typ_x, sub_x_s, sub_x_t1, sub_x_t2
      )(sub_fx_s, sub_fx_t1, sub_fx_t2),
    )(typ_sub_x)
  }

  def concrete_f_rec(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_fx: Typ,
    typ_sub_x: Typ,
  ): Typ =
    match_typ(
      case_unk0 = i => concrete_f_unk0(typ_f, typ_x, typ_sub_x, i),
      case_typ0 = s => concrete_f_typ0(typ_f, typ_x, typ_sub_x, s),
      case_typ1 = (s, t1) => concrete_f_typ1(typ_f, typ_x, typ_sub_x, s, t1),
      case_typ2 = (s, t1, t2) => concrete_f_typ2(
        typ_f, typ_x, typ_sub_x,
        s, t1, t2,
      ),
    )(typ_sub_fx)

  // may have sync conflicts
  def concrete_f(
    typ_f: Typ,
    typ_x: Typ,
  ): Typ =
    match_typ(
      case_unk0 = _s => T_Func(typ_x, T_A0),
      case_typ0 = _s => rtFail(s"Unexpected typ_f `$typ_f`."),
      case_typ1 = (_s, _t1) => rtFail(s"Unexpected typ_f `$typ_f`."),
      case_typ2 = (_s, t1, _t2) => concrete_f_rec(typ_f, typ_x, t1, typ_x),
    )(typ_f)

  def continue_typifying_call_1_with_unknown_f(
    typified_f: Typified,
    typified_x: Typified,
  ) = {
    rt_assert_type_Unk0(typified_f.typ)

    val new_typ_f = match_typ(
      case_unk0 = i => T_Func(Unk0(i), Unk0(i + 1)),
      case_typ0 = s => T_Func(Typ0(s), T_A0),
      case_typ1 = (s, t1) => T_Func(Typ1(s, increase_unk(t1)), T_A0),
      case_typ2 = (s, t1, t2) => T_Func(
        Typ2(s, increase_unk(t1), increase_unk(t2)), T_A0)
    )(typified_x.typ)

    val new_typified_f = replace_typ(typified_f, new_typ_f)
    TypifiedCall1(new_typified_f, typified_x, new_typ_f.t2)
  }

  def continue_typifying_call_1_with_unknown_x(
    typified_f: Typified,
    typified_x: Typified,
  ) = {
    val typified_f_typ = rt_assert_type_Typ2(typified_f.typ) // todo - try better typing
    val new_typified_x = replace_typ(typified_x, typified_f_typ.t1)
    TypifiedCall1(typified_f, new_typified_x, typified_f_typ.t2)
  }

  def continue_typifying_call_1(
    typified_f: Typified,
    typified_x: Typified,
  ) = {
    val new_typ_f = concrete_f(typified_f.typ, typified_x.typ)
    val new_typ2_f = rt_assert_type_Typ2(new_typ_f) // todo - try better typing
    val new_typified_f = replace_typ(typified_f, new_typ2_f)
    val new_typified_x = replace_typ(typified_x, new_typ2_f.t1)
    TypifiedCall1(new_typified_f, new_typified_x, new_typ2_f.t2)
  }

  def typify_set_call_1(
    expr_f: Expr,
    expr_x: Expr,
  ) =
    (for {
      typified_f <- typify_set(expr_f)
      _ = rt_assert(
        (typified_f.typ.isInstanceOf[Typ2]
          && typified_f.typ.asInstanceOf[Typ2].s == builtin_Func // todo - try better typing
          ) || typified_f.typ.isInstanceOf[Unk0]
      )
      typified_x <- typify_set(expr_x)

      mb_current_typified_call1 = rt_try(() =>
        if (typified_f.typ.isInstanceOf[Unk0])
          continue_typifying_call_1_with_unknown_f(typified_f, typified_x)
        else if (typified_x.typ.isInstanceOf[Unk0])
          continue_typifying_call_1_with_unknown_x(typified_f, typified_x)
        else continue_typifying_call_1(typified_f, typified_x)
      )
    } yield mb_current_typified_call1)
      .collect { case Right(typified) => typified }

  def typify_set_lambda_1(
    expr_arg: Expr,
    expr_res: Expr,
  ) =
    (for {
      typified_arg <- typify_set(expr_arg)
      typified_res <- typify_set(expr_res)

      mb_res = rt_try { () =>
        val typified_arg_s = match_typified(
          case_idf = (s, _) => s,
          case_lit = (_, _) => rtFail(),
          case_call_1 = (_, _, _) => rtFail(),
          case_lambda_1 = (_, _, _) => rtFail(),
        )(typified_arg) // todo - try better typing

        val found_typ_arg = find_idf_typ(typified_res, typified_arg_s)
        val retypified_arg = replace_typ(typified_arg, found_typ_arg)

        //todo - likely, next Unk0 needed
        TypifiedLambda1(retypified_arg, typified_res, Unk0(-1))
      }
    } yield mb_res)
      .collect { case Right(typified) => typified }

  def typify_set_idf(s: String) =
    idf_to_typ.getOrElse(s, Set(T_A0))
      .map(typ => TypifiedIdf(s, typ))

  def typify_set(expr: Expr): Set[Typified] =
    match_expr(
      case_lit_str = s => Set(TypifiedLit(s, T_Str)),
      case_lit_bint = i => Set(TypifiedLit(i, T_Bint)),
      case_idf = s => typify_set_idf(s)
        .map(_.asInstanceOf[Typified]), // todo - ... mb try to use set in other way
      case_call_1 = (expr_f, expr_x) => typify_set_call_1(expr_f, expr_x)
        .map(_.asInstanceOf[Typified]), // todo - ... mb try to use set in other way
      case_lambda_1 = (expr_idf_arg, expr_res) =>
        typify_set_lambda_1(expr_idf_arg, expr_res)
          .map(_.asInstanceOf[Typified]), // todo - ... mb try to use set in other way
      case_braced = inner_expr => typify_set(inner_expr),
    )(expr)

  def typify(
    expr: Expr,
  ): Typified = {
    val typified_set = typify_set(expr)
    typified_set.toList match {
      case head :: Nil => head
      case _ => rtFail(typified_set.toString)
    }
  }

  def full_typify(code: String) =
    typify(full_parse(code))
}
