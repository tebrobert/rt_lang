package lang

import lang.RtLib_0_0_Lits.builtin_Func
import utils.RtFail.rtFail

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

    def unsafe_match_type[A](
        lazy_for_type0: () => A,
        lazy_for_unk0: () => A,
        lazy_for_type1: () => A,
        lazy_for_type2: () => A,
    ): Typ => A =
        (typ: Typ) => typ match
            case Typ0(s) => lazy_for_type0()
            case Typ1(s, t1) => lazy_for_type1()
            case Typ2(s, t1, t2) => lazy_for_type2()
            case Unk0(i) => lazy_for_unk0()
            case _ => rtFail(s"Value $typ ${type typ} is not a type")


    def match_typ[A](
        case_unk0: Int => A,
        case_typ0: String => A,
        case_typ1: (String, Typ) => A,
        case_typ2: (String, Typ, Typ) => A,
    ): Typ => A =
        typ => typ match
            case Typ0(s) => case_typ0(s)
            case Unk0(i) => case_unk0(i)
            case Typ1(s, t1) => case_typ1(s, t1)
            case Typ2(s, t1, t2) => case_typ2(s, t1, t2)
            case _ => rtFail(s"Value $typ ${type typ} is not a typ.")

    def update_typ(typ_from: Typ, typ_to: Typ): Typ => Typ =
        (typ: Typ) => {
            val continue_updating = update_typ(typ_from, typ_to)

            match_typ(
                case_unk0 = _i => if (typ == typ_from) typ_to else typ,
                case_typ0 = s => Typ0(s),
                case_typ1 = (s, t1) => Typ1(s, continue_updating(t1)),
                case_typ2 = (s, t1, t2) => Typ2(s,
                    continue_updating(t1),
                    continue_updating(t2),
                ),
            )(typ)
        }

    def increase_unk(typ: Typ): Typ =
        match_typ(
            case_unk0 = i => Unk0(i + 1),
            case_typ0 = s => Typ0(s),
            case_typ1 = (s, t1) => Typ1(s, increase_unk(t1)),
            case_typ2 = (s, t1, t2) => Typ2(s,
                increase_unk(t1),
                increase_unk(t2)
            ),
        )(typ)
}
