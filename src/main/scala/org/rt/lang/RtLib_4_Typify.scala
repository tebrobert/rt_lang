package org.rt.lang

import org.rt.lang.RtLib_0_1_Types.Typ

object RtLib_4_Typify {
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
  ) extends Typified {
    def apply(typified_f: Typified, typified_x: Typified, typ: Typ): TypifiedCall1 =
      ??? ///  todo
  }

  final case class TypifiedLambda1(
    typified_idf_x: Typified,
    typified_res: Typified,
    typ:Option[Typ] = None,
  ) extends Typified {
    def apply(typified_idf_x: Typified, typified_res: Typified, typ:Option[Typ] = None): TypifiedLambda1 =
      ??? ///  todo
  }
}
