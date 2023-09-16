from lang.lib_4_sem import *


class BuildErr(ValueError):
    def __init__(self, msg):
        self.msg = f"BuildErr: {msg}"

    def __repr__(self):
        return self.msg


class CallableShowableLambda_1:
    def __init__(self, f, s):
        if not callable(f):
            fail(ValueError("f should be callable"))
        if not type(s) is str:
            fail(ValueError("s should be a string"))
        self.s, self.f = s, f

    def __call__(self, arg):
        return self.f(arg)

    def __repr__(self):
        return self.s

    def runnable(self):
        return False


class Input:
    def __repr__(self):
        return 'Input'

    def runnable(self):
        return True


class Print:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Print("{self.s}")'

    def runnable(self):
        return True


class Flatmap:
    def __init__(self, a_fb, fa):
        if not callable(a_fb):
            fail(ValueError("a_fb should be callable"))
        if not fa.runnable():
            fail(ValueError(
                f"fa should be runnable but it is {fa=}, also {a_fb=}"
            ))
        self.a_fb, self.fa = a_fb, fa

    def __repr__(self):
        if not type(self.a_fb) is CallableShowableLambda_1:
            fail(BuildErr("Cannot show lambda"))
        return f'Flatmap({self.a_fb}, {self.fa})'

    def runnable(self):
        return True


class Pure:
    def __init__(self, a):
        self.a = a

    def __repr__(self):
        return f"Pure({self.a})"

    def runnable(self):
        return True


def showTypedLit(tlit):
    if tlit.typ == T_Str:
        return f"\"{tlit.s}\""
    return fail(BuildErr(f"Unexpected literal `{tlit}`"))


def showTypedIdf(tidf, lambArgStack):
    if tidf.s == builtin_input:
        return "Input()"
    if tidf.s == builtin_print:
        return "(lambda s: Print(s))"
    if tidf.s == builtin_flatmap:
        return "(lambda a_fb: lambda fa: Flatmap(a_fb, fa))"
    if tidf.s == builtin_pure:
        return "(lambda a: Pure(a))"
    if tidf.s in lambArgStack:
        return tidf.s
    return fail(BuildErr(f"Unexpected identifier `{tidf.s}`"))


def show(typed, lambArgStack=[]):
    if type(typed) is TypedLit:
        return showTypedLit(typed)
    if type(typed) is TypedIdf:
        return showTypedIdf(typed, lambArgStack)
    if type(typed) is TypedCall1:
        tcall = typed
        shown_f = show(tcall.typed_f, lambArgStack)
        shown_x = show(tcall.typed_x, lambArgStack)
        return f"({shown_f})({shown_x})"
    if type(typed) is TypedLambda1:
        tlamb = typed
        s = tlamb.t_idf_x.s
        return f"(lambda {s}: {show(tlamb.typed_res, [s] + lambArgStack)})"
    fail(BuildErr(f"Unexpected typed expression {typed}"))


def build(shown):
    return eval(shown)
