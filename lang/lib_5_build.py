from lang.lib_4_typify import *
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
    fail_if(typ != T_Str, f"Unexpected literal `{TypifiedLit(s, typ)}`")
    return f"\"{s}\""


@tailrec
def operator_to_latin_idf(idf_s, acc):
    return match_list(
        case_empty=lambda: acc,
        case_nonempty=lambda head, tail: rec(tail, acc + char_to_latin[head]),
    )(idf_s)


@tailrec
def to_latin_idf(idf_s):
    return (
        ("identifier_" + idf_s)
        if all(map(lambda char: char not in char_to_latin.keys(), idf_s)) else
        operator_to_latin_idf(idf_s, acc="operator_")
    )


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
    shown_f = build_str_py(typed_f, lamb_arg_stack)
    shown_x = build_str_py(typed_x, lamb_arg_stack)
    return f"({shown_f})({shown_x})"


def show_typed_lambda_1(t_idf_x, typed_res, lamb_arg_stack):
    s = t_idf_x.s
    return f"(lambda {s}: {build_str_py(typed_res, [s] + lamb_arg_stack)})"


def build_str_py(typed, lamb_arg_stack=[]):
    return match_typified(
        case_lit=lambda s, typ: show_typed_lit(s, typ),
        case_idf=lambda s, _typ: show_typed_idf(s, lamb_arg_stack),
        case_call_1=lambda typed_f, typed_x, _typ: show_typed_call_1(
            typed_f, typed_x, lamb_arg_stack
        ),
        case_lambda_1=lambda t_idf_x, typed_res, _typ: show_typed_lambda_1(
            t_idf_x, typed_res, lamb_arg_stack
        ),
    )(typed)


def full_build_str_py(code):
    return build_str_py(full_typify(code))


def build_py(str_py):
    return eval(str_py)


def full_build_py(code):
    return build_py(full_build_str_py(code))


_s = "s"
_a_fb = "a_fb"
_fa = "fa"
_a = "a"
_right = "right"
_left = "left"
