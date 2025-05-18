package org.rt.lang

import org.rt.lang.RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_1_Types.{Typ, Typ2, Unk0}
import org.rt.lang.RtLib_0_2_Builtins.T_Func
import org.rt.utils.RtFail.{rtFail, rt_assert}

object RtLib_4_Typify {
  sealed trait Typified {
    val typ: Typ
  }

  final case class TypifiedLit(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedIdf(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedCall1 private (
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

  final case class TypifiedLambda1(
    typified_idf_x: Typified,
    typified_res: Typified,
    typ: Typ,
  ) extends Typified {
    def apply(typified_idf_x: Typified, typified_res: Typified, typ: Typ): TypifiedLambda1 = {
      typified_idf_x match {
        case TypifiedIdf(_, _) => () // perhaps: specify arg type
        case _ => rtFail()
      }

      typ match {
        case Unk0(-1) => () // maybe typ is always Func2
        case Typ2(`builtin_Func`, _, _) => ()
        case _ => ???
      }

      TypifiedLambda1(
        typified_idf_x,
        typified_res,
        if (typ != Unk0(-1))
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


  //remaining: 20
}
