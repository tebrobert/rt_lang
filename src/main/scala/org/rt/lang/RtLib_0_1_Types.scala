package org.rt.lang

import RtLib_0_0_Lits.builtin_Func
import org.rt.lang.RtLib_0_2_Builtins.T_Func
import org.rt.utils.RtFail.rtFail

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

    def tTo(resultTyp: Typ) =
      T_Func(typ, resultTyp)

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
}
