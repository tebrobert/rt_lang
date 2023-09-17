from lang.lib_0_0_lits import *


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


def has_unknown(typ):
    return {
        Unknown0: lambda: True,
        Type0: lambda: False,
        Type1: lambda: has_unknown(typ.t1),
        Type2: lambda: has_unknown(typ.t1) or has_unknown(typ.t2),
    }[type(typ)]()


def concrete(typ, typ_from, typ_to):
    return {
        Unknown0: lambda: typ_to if typ == typ_from else typ,
        Type0: lambda: typ,
        Type1: lambda: Type1(typ.s, concrete(typ.t1, typ_from, typ_to)),
        Type2: lambda: Type2(typ.s,
            concrete(typ.t1, typ_from, typ_to),
            concrete(typ.t2, typ_from, typ_to)
        ),
    }[type(typ)]()
