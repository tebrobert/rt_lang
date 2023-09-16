from lang.lib_4_sem import *


class BuildErr(ValueError):
    def __init__(self, msg):
        self.msg = f"BuildErr: {msg}"

    def __repr__(self):
        return self.msg


class CallableShowableLambda1:
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
        if not type(self.a_fb) is CallableShowableLambda1:
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


def show_typed_lit(t_lit):
    if t_lit.typ == T_Str:
        return f"\"{t_lit.s}\""
    return fail(BuildErr(f"Unexpected literal `{t_lit}`"))


def show_typed_idf(t_idf, lamb_arg_stack):
    if t_idf.s == builtin_input:
        return "Input()"
    if t_idf.s == builtin_print:
        return "(lambda s: Print(s))"
    if t_idf.s == builtin_flatmap:
        return "(lambda a_fb: lambda fa: Flatmap(a_fb, fa))"
    if t_idf.s == builtin_pure:
        return "(lambda a: Pure(a))"
    if t_idf.s in lamb_arg_stack:
        return t_idf.s
    return fail(BuildErr(f"Unexpected identifier `{t_idf.s}`"))


def show(typed, immutable_lamb_arg_stack=None):
    lamb_arg_stack = [] if immutable_lamb_arg_stack is None else immutable_lamb_arg_stack
    if type(typed) is TypedLit:
        return show_typed_lit(typed)
    if type(typed) is TypedIdf:
        return show_typed_idf(typed, lamb_arg_stack)
    if type(typed) is TypedCall1:
        t_call = typed
        shown_f = show(t_call.typed_f, lamb_arg_stack)
        shown_x = show(t_call.typed_x, lamb_arg_stack)
        return f"({shown_f})({shown_x})"
    if type(typed) is TypedLambda1:
        t_lamb = typed
        s = t_lamb.t_idf_x.s
        return f"(lambda {s}: {show(t_lamb.typed_res, [s] + lamb_arg_stack)})"
    fail(BuildErr(f"Unexpected typed expression {typed}"))


def build(shown):
    return eval(shown)
