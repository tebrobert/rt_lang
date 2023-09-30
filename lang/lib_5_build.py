from lang.lib_4_sem import *
from lang.lib_0_2_builtins import *


class CallableShowableLambda1:
    def __init__(self, f, s):
        fail_if(not callable(f), ValueError("f should be callable"))
        fail_if(not type(s) is str, ValueError("s should be a string"))
        self.s, self.f = s, f

    def __call__(self, arg):
        return self.f(arg)

    def __repr__(self):
        return self.s


class Input:
    def __repr__(self):
        return 'Input'


class Print:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Print("{self.s}")'


class Flatmap:
    def __init__(self, a_fb, fa):
        fail_if(not callable(a_fb), "a_fb should be callable")
        self.a_fb, self.fa = a_fb, fa

    def __repr__(self):
        fail_if(not type(self.a_fb) is CallableShowableLambda1,
            "Cannot show lambda",
        )
        return f'Flatmap({self.a_fb}, {self.fa})'


class Pure:
    def __init__(self, a):
        self.a = a

    def __repr__(self):
        return f"Pure({self.a})"


def show_typed_lit(t_lit):
    return (f"\"{t_lit.s}\""
            if t_lit.typ == T_Str else
            fail(f"Unexpected literal `{t_lit}`")
            )


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
    return fail(f"Unexpected identifier `{t_idf.s}`")


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
    fail(f"Unexpected typed expression {typed}")


def rt_compile(shown):
    return eval(shown)
