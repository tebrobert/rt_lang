from lang.lib_0_0_lits import *
from utils.fail import *


class Unknown0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Unknown0 and that.s == self.s

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"


class Type0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Type0 and that.s == self.s

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
    lazy_if_unknown0,
    lazy_if_type0,
    lazy_if_type1,
    lazy_if_type2,
):
    return lambda typ: ({
        Unknown0: lazy_if_unknown0,
        Type0: lazy_if_type0,
        Type1: lazy_if_type1,
        Type2: lazy_if_type2,
    }
    .get(
        type(typ),
        lambda: fail(f"Value {typ} {type(typ)} is not a type")
    ))()


def has_unknown(typ):
    return match_type(
        lazy_if_unknown0=lambda: True,
        lazy_if_type0=lambda: False,
        lazy_if_type1=lambda: has_unknown(typ.t1),
        lazy_if_type2=lambda: has_unknown(typ.t1) or has_unknown(typ.t2),
    )(typ)


def concrete(typ, typ_from, typ_to):
    return match_type(
        lazy_if_unknown0=lambda: typ_to if typ == typ_from else typ,
        lazy_if_type0=lambda: typ,
        lazy_if_type1=lambda: Type1(typ.s, concrete(typ.t1, typ_from, typ_to)),
        lazy_if_type2=lambda: Type2(typ.s,
            concrete(typ.t1, typ_from, typ_to),
            concrete(typ.t2, typ_from, typ_to)
        ),
    )(typ)
