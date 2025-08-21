package org.rt.lang

import RtLib_0_0_Lits.builtin_Func
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
  }

  extension (typ: Typ) {
    def rtMatch[A](
      caseUnk0: Unk0 => A,
      caseTyp0: Typ0 => A,
      caseTyp1: Typ1 => A,
      case_typ2: (String, Typ, Typ) => A,
    ): A =
      typ match
        case unk0: Unk0 => caseUnk0(unk0)
        case typ0: Typ0 => caseTyp0(typ0)
        case typ1: Typ1 => caseTyp1(typ1)
        case Typ2(s, t1, t2) => case_typ2(s, t1, t2)

    def clarifyUnk(
      unk_from: Unk0,
      typ_to: Typ,
    ): Typ =
      typ.rtMatch(
        caseUnk0 = unk0 => if (unk0 == unk_from) typ_to else typ,
        caseTyp0 = identity,
        caseTyp1 = _.mapT1(_.clarifyUnk(unk_from, typ_to)),
        case_typ2 = (s, t1, t2) => Typ2(s,
          t1.clarifyUnk(unk_from, typ_to),
          t2.clarifyUnk(unk_from, typ_to),
        ),
      )
  }

  def increase_unk(typ: Typ): Typ =
    typ.rtMatch(
      caseUnk0 = unk0 => Unk0(unk0.i + 1),
      caseTyp0 = identity,
      caseTyp1 = _.mapT1(increase_unk),
      case_typ2 = (s, t1, t2) => Typ2(s,
        increase_unk(t1),
        increase_unk(t2)
      ),
    )
}
