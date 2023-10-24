from lang.lib_4_sem import *
from lang.lib_0_2_builtins import *


class BrickInput:
    def __repr__(self):
        return "BrickInput()"


class BrickPrint:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f"BrickPrint({self.s})"


class BrickFlatmap:
    def __init__(self, a_fb, fa):
        self.a_fb, self.fa = a_fb, fa

    def __repr__(self):
        return f"BrickFlatmap({self.a_fb}, {self.fa})"


class BrickPure:
    def __init__(self, a):
        self.a = a

    def __repr__(self):
        return f"BrickPure({self.a})"


def match_brick(
    lazy_for_input,
    lazy_for_print,
    lazy_for_flatmap,
    lazy_for_pure,
):
    return lambda typed: ({
        BrickInput: lazy_for_input,
        BrickPrint: lazy_for_print,
        BrickFlatmap: lazy_for_flatmap,
        BrickPure: lazy_for_pure,
    }
    .get(
        type(typed),
        lambda: fail(f"Value {typed} {type(typed)} is not a Brick")
    ))()


def show_typed_lit(t_lit):
    fail_if(t_lit.typ != T_Str, f"Unexpected literal `{t_lit}`")
    return f"\"{t_lit.s}\""


def show_typed_idf(t_idf, lamb_arg_stack):
    return (
        t_idf.s if t_idf.s in lamb_arg_stack else
        match_builtin_idf(
            case_input=lambda: f"{BrickInput()}",
            case_print=lambda: f"(lambda {_s}: {BrickPrint(_s)})",
            case_flatmap=(
                lambda: f"(lambda {_a_fb}: lambda {_fa}: "
                        + f"{BrickFlatmap(_a_fb, _fa)})"
            ),
            case_pure=lambda: f"(lambda {_a}: {BrickPure(_a)})",
            case_plus=(
                lambda: f"(lambda {_right}: lambda {_left}: "
                        + f"{_left} + {_right})"
            )
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


_s = "s"
_a_fb = "a_fb"
_fa = "fa"
_a = "a"
_right = "right"
_left = "left"
