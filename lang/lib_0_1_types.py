from lang.lib_0_0_lits import *
from utils.fail import *


class Type0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Type0 and that.s == self.s

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"


class Unknown0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Unknown0 and that.s == self.s

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"


class Type1:
    def __init__(self, s, t1):
        self.s, self.t1 = s, t1

    def __eq__(self, that):
        return (type(that) == Type1
                and (that.s, that.t1) == (self.s, self.t1)
                )

    def __repr__(self, indent=""):
        return f"{indent}{self.s}[{self.t1}]"


class Type2:
    def __init__(self, s, t1, t2):
        self.s, self.t1, self.t2 = s, t1, t2

    def __eq__(self, that):
        return (type(that) == Type2
                and (that.s, that.t1, that.t2) == (self.s, self.t1, self.t2)
                )

    def __repr__(self, indent=""):
        return indent + (
            f"({self.t1}) => {self.t2}"
            if self.s == builtin_Func and self.t1.s == builtin_Func
            else f"{self.t1} => {self.t2}"
            if self.s == builtin_Func
            else f"{self.s}[{self.t1}, {self.t2}]"
        )


def match_type(
    lazy_for_type0,
    lazy_for_unknown0,
    lazy_for_type1,
    lazy_for_type2,
):
    return lambda typ: ({
        Type0: lazy_for_type0,
        Unknown0: lazy_for_unknown0,
        Type1: lazy_for_type1,
        Type2: lazy_for_type2,
    }
    .get(
        type(typ),
        lambda: fail(f"Value {typ} {type(typ)} is not a type")
    ))()


def concrete(typ, typ_from, typ_to):
    return match_type(
        lazy_for_unknown0=lambda: typ_to if typ == typ_from else typ,
        lazy_for_type0=lambda: typ,
        lazy_for_type1=lambda: Type1(typ.s, concrete(typ.t1, typ_from, typ_to)),
        lazy_for_type2=lambda: Type2(typ.s,
            concrete(typ.t1, typ_from, typ_to),
            concrete(typ.t2, typ_from, typ_to)
        ),
    )(typ)
