package org.rt.lang

import RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.utils.RtFail.*

object RtLib_0_1_Types {
  sealed trait Typ

  final case class Typ0(s: String) extends Typ {
    def repr(indent: String = ""): String =
      s"$indent$s"
  }

  final case class Unk0(i: Int) extends Typ {
    def repr(indent: String = ""): String =
      s"${indent}A$i"
  }

  final case class Typ1(s: String, t1: Typ) extends Typ {
    def repr(indent: String = ""): String =
      s"$indent$s[$t1]"

    def mapT1(f: Typ => Typ): Typ1 = Typ1(s, f(t1))
  }

  final case class Typ2(s: String, t1: Typ, t2: Typ) extends Typ {
    def repr(indent: String = ""): String =
      indent + (t1 match
        case Typ2(builtin_Func, _, _) if s == builtin_Func =>
          s"($t1) => $t2"
        case _ if s == builtin_Func =>
          s"$t1 => $t2"
        case _ => s"$s[$t1, $t2]"
        )

    def mapT1(f: Typ => Typ): Typ2 = Typ2(s, f(t1), t2)

    def mapT2(f: Typ => Typ): Typ2 = Typ2(s, t1, f(t2))
  }

  extension (typ: Typ) {
    def rtMatch[A](
      caseUnk0: Unk0 => A,
      caseTyp0: Typ0 => A,
      caseTyp1: Typ1 => A,
      caseTyp2: Typ2 => A,
    ): A =
      typ match
        case unk0: Unk0 => caseUnk0(unk0)
        case typ0: Typ0 => caseTyp0(typ0)
        case typ1: Typ1 => caseTyp1(typ1)
        case typ2: Typ2 => caseTyp2(typ2)

    def rtMatch[A](
      caseUnk0: Unk0 => A,
      otherwise: () => A,
    ): A =
      rtMatch(
        caseUnk0 = caseUnk0,
        caseTyp0 = _ => otherwise(),
        caseTyp1 = _ => otherwise(),
        caseTyp2 = _ => otherwise(),
      )

    def clarifyUnk(
      unk_from: Unk0,
      typ_to: Typ,
    ): Typ =
      typ.rtMatch(
        caseUnk0 = unk0 => if (unk0 == unk_from) typ_to else typ,
        caseTyp0 = identity,
        caseTyp1 = _.mapT1(_.clarifyUnk(unk_from, typ_to)),
        caseTyp2 = _
          .mapT1(_.clarifyUnk(unk_from, typ_to))
          .mapT2(_.clarifyUnk(unk_from, typ_to)),
      )

    def concretizeAsFunc( // may have sync conflicts
      typ_x: Typ,
    ): Typ =
      typ.rtMatch(
        caseUnk0 = _ => typ_x tTo T_A0,
        caseTyp0 = _ => rtFail(s"Unexpected typ_f `$typ`."),
        caseTyp1 = _ => rtFail(s"Unexpected typ_f `$typ`."),
        caseTyp2 = typ2 => concretizeAsFuncRec(typ, typ_x, typ2.t1, typ_x),
      )
  }

  def increase_unk(typ: Typ): Typ =
    typ.rtMatch(
      caseUnk0 = unk0 => Unk0(unk0.i + 1),
      caseTyp0 = identity,
      caseTyp1 = _.mapT1(increase_unk),
      caseTyp2 = _
        .mapT1(increase_unk)
        .mapT2(increase_unk),
    )


  // hmmm
  def concretizeAsFuncUnk0(
    typ_f: Typ,
    typ_x: Typ,
  )(
    typ_sub_x: Typ,
    sub_fx_unk0: Unk0,
  ): Typ =
    typ_sub_x.rtMatch(
      caseUnk0 = unk0 =>
        if (unk0.i == sub_fx_unk0.i)
          typ_f
        else wip(),
      otherwise = () =>
        typ_f.clarifyUnk(sub_fx_unk0, typ_sub_x).concretizeAsFunc(typ_x),
    )

  def concretizeAsFuncTyp0(
    typ_f: Typ,
    typ_x: Typ,
  )(
    typ_sub_x: Typ,
    sub_fx_typ0: Typ0
  ) =
    typ_sub_x.rtMatch(
      caseUnk0 = unk0 =>
        typ_f.clarifyUnk(unk0, sub_fx_typ0).concretizeAsFunc(
          typ_x.clarifyUnk(unk0, sub_fx_typ0),
        ),
      caseTyp0 = typ0 => if (typ0.s == sub_fx_typ0.s) typ_f else rtFail(),
      caseTyp1 = _ => rtFail(),
      caseTyp2 = _ => rtFail(),
    )

  def concretizeAsFuncTyp1(
    typ_f: Typ,
    typ_x: Typ,
  )(
    typ_sub_x: Typ,
    sub_fx_s: String,
    sub_fx_t1: Typ,
  ) =
    typ_sub_x.rtMatch(
      caseUnk0 = _ => typ_f,
      caseTyp0 = _ => rtFail(),
      caseTyp1 = typ1 =>
        if (typ1.s == sub_fx_s)
          concretizeAsFuncRec(typ_f, typ_x, sub_fx_t1, typ1.t1)
        else rtFail(),
      caseTyp2 = _ => rtFail(),
    )

  def bad_type(
    sub_fx_s: String,
    typ_sub_x: Typ,
  ): Nothing =
    rtFail(s"Can't match the types $sub_fx_s vs $typ_sub_x")

  def concretizeAsFuncTyp2(
    typ_f: Typ,
    typ_x: Typ,
  )(
    typ_sub_x: Typ,
    sub_fx_s: String,
    sub_fx_t1: Typ,
    sub_fx_t2: Typ,
  ): Typ =
    typ_sub_x.rtMatch(
      caseUnk0 = _ => wip(),
      caseTyp0 = _ => bad_type(sub_fx_s, typ_sub_x),
      caseTyp1 = _ => bad_type(sub_fx_s, typ_sub_x),
      caseTyp2 = typ2_sub_x => {
        rt_assert_equal(typ2_sub_x.s, sub_fx_s)
        val used_t1 = concretizeAsFuncRec(typ_f, typ_x, sub_fx_t1, typ2_sub_x.t1)
        val (f1, x1) = (used_t1, rt_assert_type_Typ2(used_t1).t1) // todo - try better typing
        val used_t2 = concretizeAsFuncRec(f1, x1, sub_fx_t2, typ2_sub_x.t2)
        used_t2
      },
    )

  def concretizeAsFuncRec(
    typ_f: Typ,
    typ_x: Typ,
    typ_sub_fx: Typ,
    typ_sub_x: Typ,
  ): Typ =
    typ_sub_fx.rtMatch(
      caseUnk0 = unk0 => concretizeAsFuncUnk0(typ_f, typ_x)(typ_sub_x, unk0),
      caseTyp0 = typ0 => concretizeAsFuncTyp0(typ_f, typ_x)(typ_sub_x, typ0),
      caseTyp1 = typ1 => concretizeAsFuncTyp1(typ_f, typ_x)(typ_sub_x, typ1.s, typ1.t1),
      caseTyp2 = typ2 => concretizeAsFuncTyp2(typ_f, typ_x)(
        typ_sub_x, typ2.s, typ2.t1, typ2.t2,
      ),
    )
}
