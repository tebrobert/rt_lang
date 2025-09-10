package org.rt.lang

import RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_2_Builtins.*
import org.rt.utils.RtFail.*

object RtLib_0_1_Types {
  sealed trait Typ

  final case class Typ0(s: String) extends Typ {
    override def toString: String =
      repr()

    def repr(indent: String = ""): String =
      s"$indent$s"
  }

  final case class Unk0(i: Int) extends Typ {
    override def toString: String =
      repr()

    def repr(indent: String = ""): String =
      s"${indent}A$i"
  }

  final case class Typ1(s: String, t1: Typ) extends Typ {
    override def toString: String =
      repr()

    def repr(indent: String = ""): String =
      s"$indent$s[$t1]"

    def mapT1(f: Typ => Typ): Typ1 = Typ1(s, f(t1))
  }

  final case class Typ2(s: String, t1: Typ, t2: Typ) extends Typ {
    override def toString: String =
      repr()

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
      clarification: (Unk0, Typ),
    ): Typ = {
      val (unk_from, typ_to) = clarification

      typ.rtMatch(
        caseUnk0 = unk0 => if (unk0 == unk_from) typ_to else typ,
        caseTyp0 = identity,
        caseTyp1 = _.mapT1(_.clarifyUnk(unk_from, typ_to)),
        caseTyp2 = _
          .mapT1(_.clarifyUnk(unk_from, typ_to))
          .mapT2(_.clarifyUnk(unk_from, typ_to)),
      )
    }

    def concretizeAsFunc( // may have sync conflicts
      typ_x: Typ,
      accClarifications: List[Clarification] = Nil, // may forget to pass
    ): (Typ, List[Clarification]) =
      typ.rtMatch(
        caseUnk0 = _ => (typ_x tTo T_A0, accClarifications),
        caseTyp0 = _ => rtFail(s"Unexpected typ_f `$typ`."),
        caseTyp1 = _ => rtFail(s"Unexpected typ_f `$typ`."),
        caseTyp2 = typ2 =>
          concretizeAsFuncRec(typ, typ_x, accClarifications)(typ_x, typ2.t1),
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

  type Clarification = (Unk0, Typ)

  // hmmm
  def concretizeAsFuncUnk0(
    tF: Typ,
    tX: Typ,
    accClarifications: List[Clarification],
  )(
    tSubX: Typ,
    tSubArg: Unk0,
  ): (Typ, List[Clarification]) =
    tSubX.rtMatch(
      caseUnk0 = unk0 =>
        if (unk0.i == tSubArg.i)
          (tF, accClarifications)
        else wip("concretizeAsFuncUnk0"),
      otherwise = () => {
        tF.clarifyUnk(tSubArg, tSubX)
          .concretizeAsFunc(tX, accClarifications.appended(tSubArg -> tSubX))
      },
    )

  def concretizeAsFuncTyp0(
    tF: Typ,
    tX: Typ,
    accClarifications: List[Clarification],
  )(
    tSubX: Typ,
    tSubArg: Typ0
  ): (Typ, List[Clarification]) =
    tSubX.rtMatch(
      caseUnk0 = unk0SubX =>
        tF.clarifyUnk(unk0SubX, tSubArg)
          .concretizeAsFunc(
            tX.clarifyUnk(unk0SubX, tSubArg),
            accClarifications,
          ),
      caseTyp0 = typ0SubX =>
        if (typ0SubX.s == tSubArg.s)
          (tF, accClarifications)
        else rtFail(),
      caseTyp1 = _ => rtFail(),
      caseTyp2 = _ => rtFail(),
    )

  def concretizeAsFuncTyp1(
    tF: Typ,
    tX: Typ,
    accClarifications: List[Clarification],
  )(
    tSubX: Typ,
    tSubArg: Typ1,
  ): (Typ, List[Clarification]) =
    tSubX.rtMatch(
      caseUnk0 = _ => wip("concretizeAsFuncTyp1"), //todo - getting here on `p=print("b")\np`
      caseTyp0 = _ => rtFail(),
      caseTyp1 = typ1 =>
        if (typ1.s == tSubArg.s)
          concretizeAsFuncRec(tF, tX, accClarifications)(typ1.t1, tSubArg.t1)
        else rtFail(),
      caseTyp2 = _ => rtFail(),
    )

  def concretizeAsFuncTyp2(
    tF: Typ,
    tX: Typ,
    accClarifications: List[Clarification],
  )(
    tSubX: Typ,
    tSubArg: Typ2,
  ): (Typ, List[Clarification]) =
    tSubX.rtMatch(
      caseUnk0 = _ => wip("concretizeAsFuncTyp2"),
      caseTyp0 = _ => rtFail(s"Can't match the types $tSubArg vs $tSubX"),
      caseTyp1 = _ => rtFail(s"Can't match the types $tSubArg vs $tSubX"),
      caseTyp2 = typ2_sub_x => {
        rt_assert_equal(typ2_sub_x.s, tSubArg.s)
        val nilStub = Nil
        val (used_t1, accClarifications1) = concretizeAsFuncRec(tF, tX, nilStub)(typ2_sub_x.t1, tSubArg.t1)
        val (f1, x1) = (used_t1, rt_assert_type_Typ2(used_t1).t1) // todo - try better typing
        val (used_t2, accClarifications2) = concretizeAsFuncRec(f1, x1, nilStub)(typ2_sub_x.t2, tSubArg.t2)
        (used_t2, accClarifications++accClarifications1++accClarifications2)
      },
    )

  def concretizeAsFuncRec(
    tF: Typ,
    tX: Typ,
    accClarifications: List[Clarification],
  )(
    tSubX: Typ,
    tSubArg: Typ,
  ): (Typ, List[Clarification]) =
    tSubArg.rtMatch(
      caseUnk0 = concretizeAsFuncUnk0(tF, tX, accClarifications)(tSubX, _),
      caseTyp0 = concretizeAsFuncTyp0(tF, tX, accClarifications)(tSubX, _),
      caseTyp1 = concretizeAsFuncTyp1(tF, tX, accClarifications)(tSubX, _),
      caseTyp2 = concretizeAsFuncTyp2(tF, tX, accClarifications)(tSubX, _),
    )
}
