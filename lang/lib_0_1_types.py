from lang.lib_0_0_lits import *
from utils.fail import *


class Typ0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Typ0 and that.s == self.s

    def __hash__(self):
        return self.__repr__().__hash__()

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"


class Unk0:
    def __init__(self, i):
        self.i = i

    def __eq__(self, that):
        return type(that) == Unk0 and that.i == self.i

    def __hash__(self):
        return self.__repr__().__hash__()

    def __repr__(self, indent=""):
        return f"{indent}A{self.i}"


class Typ1:
    def __init__(self, s, t1):
        self.s, self.t1 = s, t1

    def __eq__(self, that):
        return (type(that) == Typ1
                and (that.s, that.t1) == (self.s, self.t1)
                )

    def __hash__(self):
        return self.__repr__().__hash__()

    def __repr__(self, indent=""):
        return f"{indent}{self.s}[{self.t1}]"


class Typ2:
    def __init__(self, s, t1, t2):
        self.s, self.t1, self.t2 = s, t1, t2

    def __eq__(self, that):
        return (type(that) == Typ2
                and (that.s, that.t1, that.t2) == (self.s, self.t1, self.t2)
                )

    def __hash__(self):
        return self.__repr__().__hash__()

    def __repr__(self, indent=""):
        return indent + (
            f"({self.t1}) => {self.t2}"
            if self.s == builtin_Func
               and type(self.t1) is Typ2 and self.t1.s == builtin_Func
            else f"{self.t1} => {self.t2}"
            if self.s == builtin_Func
            else f"{self.s}[{self.t1}, {self.t2}]"
        )


def unsafe_match_type(
    lazy_for_type0,
    lazy_for_unk0,
    lazy_for_type1,
    lazy_for_type2,
):
    return lambda typ: ({
        Typ0: lazy_for_type0,
        Unk0: lazy_for_unk0,
        Typ1: lazy_for_type1,
        Typ2: lazy_for_type2,
    }
    .get(
        type(typ),
        lambda: fail(f"Value {typ} {type(typ)} is not a type")
    ))()


def match_typ(
    case_unk0,
    case_typ0,
    case_typ1,
    case_typ2,
):
    return lambda typ: ({
        Unk0: lambda: case_unk0(typ.i),
        Typ0: lambda: case_typ0(typ.s),
        Typ1: lambda: case_typ1(typ.s, typ.t1),
        Typ2: lambda: case_typ2(typ.s, typ.t1, typ.t2),
    }
    .get(
        type(typ),
        lambda: fail(f"Value {typ} {type(typ)} is not a typ.")
    ))()


def update_typ(typ_from, typ_to):
    def updater(typ):
        continue_updating = update_typ(typ_from, typ_to)
        return match_typ(
            case_unk0=lambda _i: typ_to if typ == typ_from else typ,
            case_typ0=lambda s: Typ0(s),
            case_typ1=lambda s, t1: Typ1(s, continue_updating(t1)),
            case_typ2=lambda s, t1, t2: Typ2(s,
                continue_updating(t1),
                continue_updating(t2),
            ),
        )(typ)

    return updater


def increase_unk(typ):
    return match_typ(
        case_unk0=lambda i: Unk0(i+1),
        case_typ0=lambda s: Typ0(s),
        case_typ1=lambda s, t1: Typ1(s, increase_unk(t1)),
        case_typ2=lambda s, t1, t2: Typ2(s,
            increase_unk(t1),
            increase_unk(t2)
        ),
    )(typ)
