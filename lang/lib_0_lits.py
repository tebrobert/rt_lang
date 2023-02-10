from utils.fail import *

builtin_Int = "Int"
builtin_Str = "Str"
builtin_Unit = "Unit"
builtin_List = "List"
builtin_RIO = "RIO"
builtin_Func = "Func"

builtin_input = "input"
builtin_print = "print"
builtin_flatmap = "flatmap"
builtin_pure = "pure"

class LitError(ValueError):
    def __init__(self, msg):
        self.msg = msg

    def __repr__(self):
        return self.msg

class Unknown_0:
    def __init__(self, s):
        if not type(s) is str:
            fail(LitError("The args of Unknown_0 have bad types"))
        self.s = s

    def __eq__(self, that):
        return type(that) == Unknown_0 and that.s == self.s

    def hasUnknown(self):
        return True

    def concrete(self, typ_from, typ_to):
        return typ_to if self == typ_from else self

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"

class Type_0:
    def __init__(self, s):
        if not type(s) is str:
            fail(LitError("The args of Type_0 have bad types"))
        self.s = s

    def __eq__(self, that):
        return type(that) == Type_0 and that.s == self.s

    def hasUnknown(self):
        return False

    def concrete(self, _typ_from, _typ_to):
        return self

    def __repr__(self, indent=""):
        return f"{indent}{self.s}"

class Type_1:
    def __init__(self, s, t1):
        if not (type(s) is str and is_type(t1)):
            fail(LitError("The args of Type_1 have bad types"))
        self.s, self.t1 = s, t1

    def __eq__(self, that):
        return type(that) == Type_1 \
               and (that.s, that.t1) == (self.s, self.t1)

    def __repr__(self, indent=""):
        return f"{indent}{self.s}[{self.t1}]"

    def copy(self, t1):
        return Type_1(self.s, t1)

    def hasUnknown(self):
        return self.t1.hasUnknown()

    def concrete(self, typ_from, typ_to):
        return Type_1(self.s, self.t1.concrete(typ_from, typ_to))

class Type_2:
    def __init__(self, s, t1, t2):
        if not (type(s) is str and is_type(t1) and is_type(t2)):
            fail(LitError("The args of Type_2 have bad types"))
        self.s, self.t1, self.t2 = s, t1, t2

    def __eq__(self, that):
        return type(that) == Type_2 \
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
        return Type_2(self.s,
            self.t1 if t1 is None else t1,
            self.t2 if t2 is None else t2
        )

    def hasUnknown(self):
        return self.t1.hasUnknown() or self.t2.hasUnknown()

    def concrete(self, typ_from, typ_to):
        return Type_2(self.s,
            self.t1.concrete(typ_from, typ_to),
            self.t2.concrete(typ_from, typ_to)
        )

def is_type(o):
    return "hasUnknown" in o.__dir__()

T_Int = Type_0(builtin_Int)
T_Str = Type_0(builtin_Str)
T_Unit = Type_0(builtin_Unit)
T_List = lambda t1: Type_1(builtin_List, t1)
T_RIO = lambda t1: Type_1(builtin_RIO, t1)
T_Func = lambda t1, t2: Type_2(builtin_Func, t1, t2)

T_A = Unknown_0("A")
T_B = Unknown_0("B")

types = {
    builtin_Int: T_Int,
    builtin_Str: T_Str,
    builtin_Unit: T_Unit,
    builtin_List: T_List,
    builtin_RIO: T_RIO,
    builtin_Func: T_Func,
}

idf_to_type = {
    builtin_input: T_RIO(T_Str),
    builtin_print: T_Func(T_Str, T_RIO(T_Unit)),
    builtin_flatmap: T_Func(
        T_Func(T_A, T_RIO(T_B)),
        T_Func(T_RIO(T_A), T_RIO(T_B))
    ),
    builtin_pure: T_Func(T_A, T_RIO(T_A))
}