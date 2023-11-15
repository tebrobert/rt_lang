from lang.lib_3_parse import *
from lang.lib_0_2_builtins import *


class TypifiedLit:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        if expr_repr_flat():
            pass
        return f"""{indent}Typed_Lit("{self.s}", {self.typ})"""


class TypifiedIdf:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        if expr_repr_flat():
            pass
        return f"""{indent}Typed_Idf("{self.s}", {self.typ})"""


class TypifiedCall1:
    def __init__(self, typified_f, typified_x, typ):
        rt_assert(
            type(typified_f) in [TypifiedLit, TypifiedIdf, TypifiedLambda1,
                TypifiedCall1])
        rt_assert(
            type(typified_x) in [TypifiedLit, TypifiedIdf, TypifiedLambda1,
                TypifiedCall1])
        rt_assert(type(typ) in [Unk0, Typ0, Typ1, Typ2])
        rt_assert(
            type(typified_f.typ) is Unk0
            or type(typified_f.typ) is Typ2 and typified_f.typ.s == builtin_Func
        )
        rt_assert(
            Unk0 in [type(typified_f.typ.t1), type(typified_x.typ)]
            or type(typified_f.typ.t1) is type(typified_x.typ)
        )
        rt_assert(
            Unk0 in [type(typified_f.typ.t2), type(typ)]
            or type(typified_f.typ.t2) is type(typ)
        )
        self.typed_f, self.typed_x, self.typ = typified_f, typified_x, typ

    def __repr__(self, indent=""):
        shift = indent + 4 * " "
        if expr_repr_flat():
            pass
        return (f"{indent}Typed_Call_1({typified_repr_endl}"
                + f"{self.typed_f.__repr__(shift)},{typified_repr_endl}"
                + f"{self.typed_x.__repr__(shift)},{typified_repr_endl}"
                + f"{self.typ.__repr__(shift)}{typified_repr_endl}"
                + f"{indent})"
                )


class TypifiedLambda1:
    def __init__(self, typified_idf_x, typified_res, typ=None):
        rt_assert(type(typified_idf_x) is TypifiedIdf)
        rt_assert(typ is None or type(typ) is Typ2 and typ.s == builtin_Func)
        self.typified_idf_x = typified_idf_x
        self.typified_res = typified_res
        self.typ = (typ
                    if typ is not None else
                    T_Func(typified_idf_x.typ, typified_res.typ)
                    )

    def __repr__(self, indent=""):
        shift = indent + 4 * " "
        if expr_repr_flat():
            pass
        return (f"{indent}Typed_Lambda_1({typified_repr_endl}"
                + f"{self.typified_idf_x.__repr__(shift)},{typified_repr_endl}"
                + f"{self.typified_res.__repr__(shift)},{typified_repr_endl}"
                + f"{self.typ.__repr__(shift)}{typified_repr_endl}"
                + f"{indent})"
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
                typified.typified_idf_x, typified.typified_res, typified.typ
            ),
        }
        .get(
            type(typified),
            lambda: fail(
                f"Value {typified} {type(typified)} is not a typed expression")
        )
    )()


def replace_typ_lambda_1(typified_idf_x, typified_res, new_typ):
    rt_assert(type(new_typ) is Typ2 and new_typ.s == builtin_Func
              or type(new_typ) is Unk0,
        f"Unexpected type `{new_typ}`.",
    )
    updated_typified_idf_x = replace_typ(typified_idf_x, new_typ.t1)
    updated_typified_res = replace_typ(typified_res, new_typ.t2)
    return TypifiedLambda1(
        updated_typified_idf_x,
        updated_typified_res,
        new_typ,
    )


def replace_typ_call_1(typed_f, typed_x, new_typ):
    return TypifiedCall1(
        # typed_f
        replace_typ(typed_f, T_Func(typed_x.typ, new_typ))
        if type(typed_f.typ) is Unk0 else
        replace_typ(typed_f, T_Func(typed_f.typ.t1, new_typ))
        if type(typed_f.typ) is Typ2 else
        fail(f"Unexpected type `{typed_f.typ}`."),
        typed_x,
        new_typ,
    )


def replace_typ(typified, new_typ):
    return match_typified(
        case_lit=lambda s, typ: TypifiedLit(s, typ),
        case_idf=lambda s, _typ: TypifiedIdf(s, new_typ),
        case_call_1=lambda typed_f, typed_x, _typ: replace_typ_call_1(
            typed_f, typed_x, new_typ,
        ),
        case_lambda_1=(
            lambda typified_idf_x, typified_res, _typ: replace_typ_lambda_1(
                typified_idf_x, typified_res, new_typ,
            )
        ),
    )(typified)


def get_unknowns_fot_typ(typ):
    return match_typ(
        case_typ0=lambda _s: set(),
        case_unk0=lambda s: set(s),
        case_typ1=lambda _s, t1: get_unknowns_fot_typ(t1),
        case_typ2=lambda _s, t1, t2: (
                get_unknowns_fot_typ(t1) + get_unknowns_fot_typ(t2)
        ),
    )(typ)


def get_unknowns_for_typified(typified):
    return match_typified(
        case_lit=lambda _s, typ: get_unknowns_fot_typ(typ),
        case_idf=lambda _s, typ: get_unknowns_fot_typ(typ),
        case_call_1=lambda _typed_f, _typed_x, typ: get_unknowns_fot_typ(typ),
        case_lambda_1=lambda _typified_idf_x, _typified_res, typ: (
            get_unknowns_fot_typ(typ)
        ),
    )(typified)


def find_idf_typ_call_1(typified_f, typified_x, s_to_find):
    lookup_by_f = find_idf_typ(typified_f, s_to_find)
    return (lookup_by_f
            if not type(lookup_by_f) is Unk0 else
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


def sync_typ0(typ_f, typ_x, typ_sub_x, sub_fx_s):
    return (
        (typ_f, typ_x)
        if type(typ_sub_x) is Typ0 and sub_fx_s == typ_sub_x.s else
        sync_typs(
            update_typ(typ_f, typ_sub_x, Typ0(sub_fx_s)),
            update_typ(typ_x, typ_sub_x, Typ0(sub_fx_s)),
        )
        if type(typ_sub_x) is Unk0 else
        fail(f"Can't match the types {Typ0(sub_fx_s)} vs {typ_sub_x}")
    )


def sync_unk0(typ_f, typ_x, typ_sub_x, sub_fx_i):
    sub_fx_unk0 = Unk0(sub_fx_i)
    return (
        sync_typs(update_typ(typ_f, sub_fx_unk0, typ_sub_x), typ_x)
        if type(typ_sub_x) is Typ0 else
        (
            (typ_f, typ_x)
            if type(typ_sub_x) is Unk0 and sub_fx_i == typ_sub_x.i else
            fail("Not implemented: solve_rec 4")
        )
        if type(typ_sub_x) is Unk0 else
        sync_typs(update_typ(typ_f, sub_fx_unk0, typ_sub_x), typ_x)
    )


def sync_typ1(typ_f, typ_x, typ_sub_x, sub_fx_s, sub_fx_t1):
    return (
        (typ_f, typ_x)
        if type(typ_sub_x) is Unk0 else
        sync_typs_rec(typ_f, typ_x, sub_fx_t1, typ_sub_x.t1)
        if type(typ_sub_x) is Typ1 and sub_fx_s == typ_sub_x.s else
        fail(f"Can't match the types `{sub_fx_t1}` vs `{typ_sub_x}.`")
    )


def sync_typ2_typ2(typ_f, typ_x, sub_x_s, sub_x_t1, sub_x_t2,
    sub_fx_s, sub_fx_t1, sub_fx_t2,
):
    rt_assert_equal(sub_x_s, sub_fx_s)
    used_t1 = sync_typs_rec(typ_f, typ_x, sub_fx_t1, sub_x_t1)
    used_t2 = sync_typs_rec(*used_t1, sub_fx_t2, sub_x_t2)
    return used_t2


def sync_typ2(
    typ_f, typ_x, typ_sub_x, sub_fx_s, sub_fx_t1, sub_fx_t2,
):
    bad_type = lambda: fail(f"Can't match the types {sub_fx_s} vs {typ_sub_x}")
    return match_typ(
        case_typ0=lambda _s: bad_type(),
        case_unk0=lambda _i: wip(),
        case_typ1=lambda _s, _t1: bad_type(),
        case_typ2=lambda sub_x_s, sub_x_t1, sub_x_t2: sync_typ2_typ2(
            typ_f, typ_x,
            sub_x_s, sub_x_t1, sub_x_t2,
            sub_fx_s, sub_fx_t1, sub_fx_t2,
        ),
    )(typ_sub_x)


def sync_typs_rec(typ_f, typ_x, typ_sub_fx, typ_sub_x):
    return match_typ(
        case_typ0=lambda s: sync_typ0(typ_f, typ_x, typ_sub_x, s),
        case_unk0=lambda i: sync_unk0(typ_f, typ_x, typ_sub_x, i),
        case_typ1=lambda s, t1: sync_typ1(typ_f, typ_x, typ_sub_x, s, t1),
        case_typ2=lambda s, t1, t2: sync_typ2(typ_f, typ_x, typ_sub_x,
            s, t1, t2,
        ),
    )(typ_sub_fx)


def sync_typs(typ_f, typ_x):  # may have sync conflicts
    return match_typ(
        case_typ0=lambda _s: fail(f"Unexpected typ_f `{typ_f}`."),
        case_unk0=lambda _s: (T_Func(typ_x, T_A), typ_x),
        case_typ1=lambda _s, _t1: fail(f"Unexpected typ_f `{typ_f}`."),
        case_typ2=lambda _s, t1, _t2: sync_typs_rec(typ_f, typ_x, t1, typ_x),
    )(typ_f)


def concrete_f(typ_f, typ_x):
    new_typ_f, _new_typ_x = sync_typs(typ_f, typ_x)
    return new_typ_f


def continue_typifying_call_1_with_unknown_x(typified_f, typified_x):
    new_typified_x = replace_typ(typified_x, typified_f.typ.t1)
    return TypifiedCall1(typified_f, new_typified_x, typified_f.typ.t2)


def continue_typifying_call_1(typified_f, typified_x):
    new_typ_f = concrete_f(typified_f.typ, typified_x.typ)
    new_typified_f = replace_typ(typified_f, new_typ_f)
    new_typified_x = replace_typ(typified_x, new_typ_f.t1)
    return TypifiedCall1(new_typified_f, new_typified_x, new_typ_f.t2)


def typify_set_call_1(expr_f, expr_x):
    typified_f_set = typify_set(expr_f)
    typified_x_set = typify_set(expr_x)

    typified_call_1_set = set()
    for typified_f in typified_f_set:
        if not (
                type(
                    typified_f.typ) is Typ2 and typified_f.typ.s == builtin_Func
                or type(typified_f.typ) is Unk0
        ):
            continue
        for typified_x in typified_x_set:
            mb_current_typified_call1 = rt_try(lambda: (
                continue_typifying_call_1_with_unknown_x(typified_f, typified_x)
                if type(typified_x.typ) is Unk0 else
                continue_typifying_call_1(typified_f, typified_x)
            ))
            if not is_fail(mb_current_typified_call1):
                typified_call_1_set.add(mb_current_typified_call1)

    return typified_call_1_set


def typify_set_lambda_1(expr_arg, expr_res):
    typified_arg_set = typify_set(expr_arg)
    typified_res_set = typify_set(expr_res)

    res_set = set()
    for typified_arg in typified_arg_set:
        for typified_res in typified_res_set:
            def action():
                found_typ_arg = find_idf_typ(typified_res, typified_arg.s)
                retypified_arg = replace_typ(typified_arg, found_typ_arg)
                return TypifiedLambda1(retypified_arg, typified_res)

            mb_res = rt_try(action)
            if not is_fail(mb_res):
                res_set.add(mb_res)

    return res_set


def typify_set_idf(s):
    return set(map(lambda typ: TypifiedIdf(s, typ),
        idf_to_typ.get(s, {T_A})
    ))


def typify_set(expr):
    return match_expr(
        case_lit_str=lambda s: {TypifiedLit(s, T_Str)},
        case_lit_bint=lambda i: {TypifiedLit(i, T_Bint)},
        case_idf=lambda s: typify_set_idf(s),
        case_call_1=lambda expr_f, expr_x: typify_set_call_1(expr_f,
            expr_x),
        case_lambda_1=lambda expr_idf_arg, expr_res: typify_set_lambda_1(
            expr_idf_arg, expr_res
        ),
        case_braced=lambda inner_expr: typify_set(inner_expr),
    )(expr)


def typify(expr):
    typified_set = typify_set(expr)
    rt_assert(len(typified_set) == 1, str(typified_set))
    return list(typified_set)[0]


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
