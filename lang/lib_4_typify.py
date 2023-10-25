from lang.lib_3_parse import *
from lang.lib_0_2_builtins import *


class TypifiedLit:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        return f"""{indent}Typed_Lit("{self.s}", {self.typ})"""


class TypifiedIdf:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        return f"""{indent}Typed_Idf("{self.s}", {self.typ})"""


class TypifiedCall1:
    def __init__(self, typed_f, typed_x, typ):
        self.typed_f, self.typed_x, self.typ = typed_f, typed_x, typ

    def __repr__(self, indent=""):
        shift = indent + 4 * " "
        return (f"{indent}Typed_Call_1(\n"
                + f"{self.typed_f.__repr__(shift)},\n"
                + f"{self.typed_x.__repr__(shift)},\n"
                + f"{self.typ.__repr__(shift)}\n"
                + f"{indent})"
                )


class TypifiedLambda1:
    def __init__(self, t_idf_x, typed_res, typ=None):
        rt_assert(type(t_idf_x) is TypifiedIdf)
        rt_assert(typ is None or type(typ) is Typ2 and typ.s == builtin_Func)
        self.t_idf_x = t_idf_x
        self.typed_res = typed_res
        self.typ = (typ
                    if typ is not None else
                    T_Func(t_idf_x.typ, typed_res.typ)
                    )

    def __repr__(self, indent=""):
        shift = indent + 4 * " "
        return (f"{indent}Typed_Lambda_1(\n"
                + f"{self.t_idf_x.__repr__(shift)},\n"
                + f"{self.typed_res.__repr__(shift)},\n"
                + f"{self.typ.__repr__(shift)}"
                + f"\n{indent})"
                )


def match_typified(
    case_lit,
    case_idf,
    case_call_1,
    case_lambda_1,
):
    return lambda typified: (
        {
            TypifiedLit: lambda: case_lit(typified.s, typified.typ),
            TypifiedIdf: lambda: case_idf(typified.s, typified.typ),
            TypifiedCall1: lambda: case_call_1(
                typified.typed_f, typified.typed_x, typified.typ
            ),
            TypifiedLambda1: lambda: case_lambda_1(
                typified.t_idf_x, typified.typed_res, typified.typ
            ),
        }
        .get(
            type(typified),
            lambda: fail(
                f"Value {typified} {type(typified)} is not a typed expression")
        )
    )()


def replace_typ(typified, new_typ):
    return match_typified(
        case_lit=lambda s, typ: TypifiedLit(s, typ),
        case_idf=lambda s, _typ: TypifiedIdf(s, new_typ),
        case_call_1=lambda typed_f, typed_x, _typ: TypifiedCall1(
            typed_f, typed_x, new_typ
        ),
        case_lambda_1=lambda t_idf_x, typed_res, _typ: TypifiedLambda1(
            t_idf_x, typed_res, new_typ
        ),
    )(typified)


def find_idf_typ_call_1(typified_f, typified_x, s_to_find):
    lookup_by_f = find_idf_typ(typified_f, s_to_find)
    return (lookup_by_f
            if not type(lookup_by_f) is TypUnknown0 else
            find_idf_typ(typified_x, s_to_find)
            )


def find_idf_typ(typified, s_to_find):
    return match_typified(
        case_lit=lambda _s, _typ: T_A,
        case_idf=lambda s, typ: typ if s == s_to_find else T_A,
        case_call_1=lambda typed_f, typed_x, _typ: find_idf_typ_call_1(
            typed_f, typed_x, s_to_find
        ),
        case_lambda_1=lambda _t_idf_x, typed_res, _typ: find_idf_typ(
            typed_res, s_to_find
        ),
    )(typified)


def solve_typ_0(
    typ_f, typ_x, typ_sub_fx, typ_sub_x,
    _f_x_synched_unks, synched_unks,
):
    return (
        (typ_f, typ_x, synched_unks)
        if type(typ_sub_x) is Typ0 and typ_sub_fx.s == typ_sub_x.s else
        fail("Not implemented: solve_rec 1")
        if type(typ_sub_x) is TypUnknown0 else
        fail(f"Can't match the types {typ_sub_fx} vs {typ_sub_x}")
    )


def solve_unknown_0(
    typ_f, typ_x, typ_sub_fx, typ_sub_x,
    f_x_synched_unks, synched_unks,
):
    return (
        solve(update_typ(typ_f, typ_sub_fx, typ_sub_x), typ_x)
        if type(typ_sub_x) is Typ0 else
        (
            (typ_f, typ_x, synched_unks.union({typ_sub_fx.s}))
            if typ_sub_fx.s == typ_sub_x.s else
            fail("Not implemented: solve_rec 4")
        )
        if type(typ_sub_x) is TypUnknown0 else
        fail(type_match_err_msg)
        if typ_sub_fx.s in f_x_synched_unks else
        solve(update_typ(typ_f, typ_sub_fx, typ_sub_x), typ_x)
    )


def solve_typ_1(
    typ_f, typ_x, typ_sub_fx, typ_sub_x,
    f_x_synched_unks, synched_unks,
):
    return (
        (
            fail(type_match_err_msg)
            if typ_sub_x.s in f_x_synched_unks else
            fail(
                f"Yet can't call `{typ_f}` with `{typ_x}`.",
                f"Currently matching `{typ_sub_fx}` and `{typ_sub_x}`.",
            )
        )
        if type(typ_sub_x) is TypUnknown0 else
        solve_rec(typ_sub_fx.t1, typ_sub_x.t1, (typ_f, typ_x, synched_unks))
        if type(typ_sub_x) is Typ1 else
        fail(f"Can't match the types `{typ_sub_fx}` vs `{typ_sub_x}.`")
    )


def solve_typ_2(
    typ_f, typ_x, typ_sub_fx, typ_sub_x,
    f_x_synched_unks, synched_unks,
):
    return (
        (
            fail(type_match_err_msg)
            if typ_sub_x.s in f_x_synched_unks else
            fail(
                f"Yet can't call `{typ_f}` with `{typ_x}`.",
                f"Currently matching `{typ_sub_fx}` and `{typ_sub_x}`",
            )
        )
        if type(typ_sub_x) is TypUnknown0 else
        solve_rec(typ_sub_fx.t2, typ_sub_x.t2,
            solve_rec(typ_sub_fx.t1, typ_sub_x.t1, (typ_f, typ_x, synched_unks))
        )
        if type(typ_sub_x) is Typ2 else
        fail(f"Can't match the types {typ_sub_fx} vs {typ_sub_x}")
    )


def solve_rec(typ_sub_fx, typ_sub_x, f_x_synched_unks):
    typ_f, typ_x, synched_unks = f_x_synched_unks

    return match_typ(
        case_typ0=lambda _s: solve_typ_0,
        case_unknown0=lambda _s: solve_unknown_0,
        case_typ1=lambda _s, _t1: solve_typ_1,
        case_typ2=lambda _s, _t1, _t2: solve_typ_2,
    )(typ_sub_fx)(
        typ_f, typ_x,
        typ_sub_fx, typ_sub_x,
        f_x_synched_unks, synched_unks,
    )


def solve(typ_f, typ_x): # may have sync conflicts
    fail_not_a_func = lambda: fail(
        f"typ_f `{typ_f}` should be a `{builtin_Func}`"
    )

    return match_typ(
        case_typ0=lambda _s: fail_not_a_func(),
        case_unknown0=lambda _s: (T_Func(typ_x, T_A), typ_x, set()),
        case_typ1=lambda _s, _t1: fail_not_a_func(),
        case_typ2=lambda _s, t1, _t2: solve_rec(t1, typ_x, (typ_f, typ_x, set())),
    )(typ_f)


def concreted(typ_f, typ_x):
    new_typ_f, _new_typ_x, _synched_unks = solve(typ_f, typ_x)
    return new_typ_f


def continue_typifying_call_1_with_unknown_x(typified_f, typified_x):
    new_typified_x = replace_typ(typified_x, typified_f.typ.t1)
    return TypifiedCall1(typified_f, new_typified_x, typified_f.typ.t2)


def continue_typifying_call_1(typified_f, typified_x):
    new_typ_f = concreted(typified_f.typ, typified_x.typ)
    new_typified_f = replace_typ(typified_f, new_typ_f)
    new_typified_x = replace_typ(typified_x, new_typ_f.t1)
    return TypifiedCall1(new_typified_f, new_typified_x, new_typ_f.t2)


def typify_call_1(expr_f, expr_x):
    typified_f = typify(expr_f)
    typified_x = typify(expr_x)

    rt_assert(
        type(typified_f.typ) is Typ2 and typified_f.typ.s == builtin_Func
        or type(typified_f.typ) is TypUnknown0,
        f"typified_f should be a `{builtin_Func}`",
    )

    return (
        continue_typifying_call_1_with_unknown_x(typified_f, typified_x)
        if type(typified_x.typ) is TypUnknown0 else
        continue_typifying_call_1(typified_f, typified_x)
    )


def typify_lambda_1(expr_arg, expr_res):
    typified_arg = typify(expr_arg)
    typified_res = typify(expr_res)

    found_typ_arg = find_idf_typ(typified_res, typified_arg.s)

    retypified_arg = replace_typ(typified_arg, found_typ_arg)

    return TypifiedLambda1(retypified_arg, typified_res)


def typify(expr):
    return match_expr(
        case_lit_str=lambda s: TypifiedLit(s, T_Str),
        case_idf=lambda s: TypifiedIdf(s, idf_to_type.get(s, T_A)),
        case_call_1=lambda expr_f, expr_x: typify_call_1(expr_f, expr_x),
        case_lambda_1=lambda expr_idf_arg, expr_res: typify_lambda_1(
            expr_idf_arg, expr_res
        ),
    )(expr)


def full_typify(code):
    return typify(full_parse(code))


type_match_err_msg = (
    f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]"
)

"""
solve(f, x) => (updated_f, updated_x, updated_synched_unks)
= solve_rec(f.x, x, (f, x, empty))

solve_rec(sub_fx, sub_x, (f, x, synched_unks)) => (updated_f, updated_x, updated_synched_unks)
= sub_fx      sub_x       return
  Type0_A     Type0_A     (f, x, synched_unks)
  Type0_A     Unk0_A      solve(f, x.repl(Unk0_A, Type_0))
  Unk0_A      Type0_A     solve(f.repl(Unk0_A, Type0_A), x)
  Unk0_A      Unk0_A      (f, x, synched_unks.add(Unk0_A))
  Unk0_A      Unk0_B      solve(f, x.swap(Unk0_B, Unk0_A), synched_unks.add(Unk0_A))
  Unk0_A      sub_x       too_much(Unk0_A, sub_x, synched_unks)
  Type1       Unk0_A      too_much(Unk0_A, Type1, synched_unks)
  Type1(a)    Type1(b)    solve_rec(a, b, (f, x, synched_unks))
  Type2       Unk0_A      too_much(Unk0_A, Type2, synched_unks)
  Type2(a, b) Type2(c, d) solve_rec(b, d, solve_rec(a, c, (x, f, synched_unks)))

too_much(unk0, typ, synched_unks) => Nothing
= if Unk0_A in Type2:
      if Unk0_A in synched_unks:
          err #remember the case A=>A vs A=>{builtin_List}[A]
      else: exc # A => (B, C) vs (A, B) => C to (A, B) => (C, D) - looks too complicated and unlikely
"""
