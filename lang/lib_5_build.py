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
    case_input,
    case_print,
    case_flatmap,
    case_pure,
):
    return lambda brick: ({
        BrickInput: case_input,
        BrickPrint: lambda: case_print(brick.s),
        BrickFlatmap: lambda: case_flatmap(brick.a_fb, brick.fa),
        BrickPure: lambda: case_pure(brick.a),
    }
    .get(
        type(brick),
        lambda: fail(f"Value {brick} {type(brick)} is not a Brick")
    ))()


def show_typed_lit(s, typ):
    fail_if(typ != T_Str, f"Unexpected literal `{TypedLit(s, typ)}`")
    return f"\"{s}\""


def show_typed_idf(s, lamb_arg_stack):
    return (
        s if s in lamb_arg_stack else
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
        )(s)
    )


def show_typed_call_1(typed_f, typed_x, lamb_arg_stack):
    shown_f = build(typed_f, lamb_arg_stack)
    shown_x = build(typed_x, lamb_arg_stack)
    return f"({shown_f})({shown_x})"


def show_typed_lambda_1(t_idf_x, typed_res, lamb_arg_stack):
    s = t_idf_x.s
    return f"(lambda {s}: {build(typed_res, [s] + lamb_arg_stack)})"


def build(typed, lamb_arg_stack=[]):
    return match_typed(
        case_lit=lambda s, typ: show_typed_lit(s, typ),
        case_idf=lambda s, _typ: show_typed_idf(s, lamb_arg_stack),
        case_call_1=lambda typed_f, typed_x, _typ: show_typed_call_1(
            typed_f, typed_x, lamb_arg_stack
        ),
        case_lambda_1=lambda t_idf_x, typed_res, _typ: show_typed_lambda_1(
            t_idf_x, typed_res, lamb_arg_stack
        ),
    )(typed)


def full_build(code):
    return build(sem(full_parse(code)))


def rt_compile(shown):
    return eval(shown)


_s = "s"
_a_fb = "a_fb"
_fa = "fa"
_a = "a"
_right = "right"
_left = "left"
