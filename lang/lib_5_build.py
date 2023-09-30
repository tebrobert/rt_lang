from lang.lib_4_sem import *
from lang.lib_0_2_builtins import *


class CallableShowableLambda1:
    def __init__(self, f, s):
        fail_if(not callable(f), "f should be callable")
        fail_if(not type(s) is str, "s should be a string")
        self.s, self.f = s, f

    def __call__(self, arg):
        return self.f(arg)

    def __repr__(self):
        return self.s


def show_typed_lit(t_lit):
    return (f"\"{t_lit.s}\"" if t_lit.typ == T_Str else
            fail(f"Unexpected literal `{t_lit}`")
            )


def show_typed_idf(t_idf, lamb_arg_stack):
    return (
        t_idf.s if t_idf.s in lamb_arg_stack else
        match_builtin_idf(
            lazy_for_input=lambda: "Input()",
            lazy_for_print=lambda: "(lambda s: Print(s))",
            lazy_for_flatmap=(
                lambda: "(lambda a_fb: lambda fa: Flatmap(a_fb, fa))"
            ),
            lazy_for_pure=lambda: "(lambda a: Pure(a))",
        )(t_idf.s)
    )


def show_typed_call_1(t_call, lamb_arg_stack):
    shown_f = show(t_call.typed_f, lamb_arg_stack)
    shown_x = show(t_call.typed_x, lamb_arg_stack)
    return f"({shown_f})({shown_x})"


def show_typed_lambda_1(t_lamb, lamb_arg_stack):
    s = t_lamb.t_idf_x.s
    return f"(lambda {s}: {show(t_lamb.typed_res, [s] + lamb_arg_stack)})"


def show(typed, lamb_arg_stack=[]):
    return match_typed(
        lazy_for_typed_lit=lambda: show_typed_lit(typed),
        lazy_for_typed_idf=lambda: show_typed_idf(typed, lamb_arg_stack),
        lazy_for_typed_call_1=lambda: show_typed_call_1(typed, lamb_arg_stack),
        lazy_for_typed_lambda_1=(
            lambda: show_typed_lambda_1(typed, lamb_arg_stack)
        ),
    )(typed)


def rt_compile(shown):
    return eval(shown)
