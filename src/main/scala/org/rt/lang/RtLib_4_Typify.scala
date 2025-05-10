package org.rt.lang

import org.rt.lang.RtLib_0_1_Types.Typ
import org.rt.utils.RtFail.rt_assert

object RtLib_4_Typify {
  //todo - typ? then what about TypifiedLambda1?
  sealed trait Typified

  final case class TypifiedLit(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedIdf(
    s: String,
    typ: Typ,
  ) extends Typified

  final case class TypifiedCall1(
    typified_f: Typified,
    typified_x: Typified,
    typ: Typ,
  ) extends Typified

  object TypifiedCall1 {
    def apply(typified_f: Typified, typified_x: Typified, typ: Typ): TypifiedCall1 =
      //rt_assert(typified_f.isInstanceOf[])
      new TypifiedCall1(typified_f, typified_x, typ)
  }

  final case class TypifiedLambda1(
    typified_idf_x: Typified,
    typified_res: Typified,
    typ:Option[Typ] = None,
  ) extends Typified {
    def apply(typified_idf_x: Typified, typified_res: Typified, typ:Option[Typ] = None): TypifiedLambda1 =
      ??? ///  todo
  }

  private def match_typified[A](
    case_lit: (String, Typ) => A,
    case_idf: (String, Typ) => A,
    case_call_1: (Typified, Typified, Typ) => A,
    case_lambda_1: (Typified, Typified, Option[Typ]) => A,
  ): Typified => A = {
    case TypifiedLit(s, typ) => case_lit(s, typ)
    case TypifiedIdf(s, typ) => case_idf(s, typ)
    case TypifiedCall1(typified_f, typified_x, typ) => case_call_1(typified_f, typified_x, typ)
    case TypifiedLambda1(typified_idf_x, typified_res, typ) => case_lambda_1(typified_idf_x, typified_res, typ)
  }


  //remaining: 20
}
