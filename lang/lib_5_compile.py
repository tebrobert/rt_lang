from utils.fail import *
from lang.lib_4_sem import *

class CompileErr(ValueError):
    def __init__(self, msg):
        self.msg = f"CompileErr: {msg}"

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
            fail(ValueError(f"fa should be runnable but it is {fa=}, also {a_fb=}"))

        self.a_fb, self.fa = a_fb, fa

    def __repr__(self):
        if not type(self.a_fb) is CallableShowableLambda_1:
            fail(CompileErr("Cannot show lambda"))

        return f'Flatmap({self.a_fb}, {self.fa})'

    def runnable(self):
        return True

def show(typed, lamb_arg_stack=[]):
    if type(typed) is Typed_Lit:
        tlit = typed

        if tlit.typ == T_Str:
            return f"\"{tlit.s}\""

        return fail(CompileErr(f"Unexpected literal `{tlit}`"))

    if type(typed) is Typed_Idf:
        tidf = typed

        if tidf.s == builtin_input:
            return "Input()"

        if tidf.s == builtin_print:
            return "(lambda s: Print(s))"

        if tidf.s == builtin_flatmap:
            return "(lambda a_fb: lambda fa: Flatmap(a_fb, fa))"

        if tidf.s in lamb_arg_stack:
            return tidf.s

        return fail(CompileErr(f"Unexpected identifier `{tidf.s}`"))

    if type(typed) is Typed_Call_1:
        tcall = typed
        shown_f = show(tcall.typed_f, lamb_arg_stack)
        shown_x = show(tcall.typed_x, lamb_arg_stack)
        return f"({shown_f})({shown_x})"
    
    if type(typed) is Typed_Lambda_1:
        tlamb = typed
        s = tlamb.tidf_x.s
        return f"(lambda {s}: {show(tlamb.typed_res, [s]+lamb_arg_stack)})"
    
    return fail(CompileErr(f"Unexpected typed expression {typed}"))

def compile(shown):
    return eval(shown)
