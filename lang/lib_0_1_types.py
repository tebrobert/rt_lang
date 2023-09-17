from lang.lib_0_0_lits import *
from utils.fail import *


class Unknown0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Unknown0 and that.s == self.s

    def concrete(self, typ_from, typ_to):
        return typ_to if self == typ_from else self

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"


class Type0:
    def __init__(self, s):
        self.s = s

    def __eq__(self, that):
        return type(that) == Type0 and that.s == self.s

    def concrete(self, _typ_from, _typ_to):
        return self

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

    def copy(self, t1):
        return Type1(self.s, t1)

    def concrete(self, typ_from, typ_to):
        return Type1(self.s, self.t1.concrete(typ_from, typ_to))


class Type2:
    def __init__(self, s, t1, t2):
        self.s, self.t1, self.t2 = s, t1, t2

    def __eq__(self, that):
        return type(that) == Type2 \
            and (that.s, that.t1, that.t2) == (self.s, self.t1, self.t2)

    def __repr__(self, indent=""):
        return indent + (
            f"({self.t1}) => {self.t2}"
            if self.s == builtin_Func and self.t1.s == builtin_Func
            else f"{self.t1} => {self.t2}"
            if self.s == builtin_Func
            else f"{self.s}[{self.t1}, {self.t2}]"
        )

    def copy(self, t1=None, t2=None):
        return Type2(self.s,
            self.t1 if t1 is None else t1,
            self.t2 if t2 is None else t2
        )

    def has_unknown(self):
        return has_unknown(self.t1) or has_unknown(self.t2)

    def concrete(self, typ_from, typ_to):
        return Type2(self.s,
            self.t1.concrete(typ_from, typ_to),
            self.t2.concrete(typ_from, typ_to)
        )


def is_type(o):
    return type(o) in [Unknown0, Type0, Type1, Type2]


def not_a_type(typ):
    return fail(f"The value {typ} {type(typ)} is not an type.")


def has_unknown(typ):
    return (
        True if type(typ) is Unknown0 else
        False if type(typ) is Type0 else
        has_unknown(typ.t1) if type(typ) is Type1 else
        has_unknown(typ.t1) or has_unknown(typ.t2) if type(
            typ) is Type2 else
        not_a_type(typ)
    )


def concrete(typ, typ_from, typ_to):
    return (
        (typ_to if typ == typ_from else typ) if type(typ) is Unknown0 else
        typ if type(typ) is Type0 else
        Type1(typ.s, concrete(typ.t1, typ_from, typ_to)) if type(
            typ) is Type1 else
        Type2(typ.s,
            concrete(typ.t1, typ_from, typ_to),
            concrete(typ.t2, typ_from, typ_to)
        ) if type(typ) is Type2 else
        not_a_type(typ)
    )
