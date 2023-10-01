from lang.lib_3_parse import *
from lang.lib_0_2_builtins import *


class TypedLit:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        return f"""{indent}Typed_Lit("{self.s}", {self.typ})"""


class TypedIdf:
    def __init__(self, s, typ):
        self.s, self.typ = s, typ

    def __repr__(self, indent=""):
        return f"""{indent}Typed_Idf("{self.s}", {self.typ})"""


class TypedCall1:
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


class TypedLambda1:
    def __init__(self, t_idf_x, typed_res, typ=None):
        rt_assert(type(t_idf_x) is TypedIdf)
        rt_assert(typ is None or type(typ) is Type2 and typ.s == builtin_Func)
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


def match_typed(
    lazy_for_typed_lit,
    lazy_for_typed_idf,
    lazy_for_typed_call_1,
    lazy_for_typed_lambda_1,
):
    return lambda typed: ({
        TypedLit: lazy_for_typed_lit,
        TypedIdf: lazy_for_typed_idf,
        TypedCall1: lazy_for_typed_call_1,
        TypedLambda1: lazy_for_typed_lambda_1,
    }
    .get(
        type(typed),
        lambda: fail(f"Value {typed} {type(typed)} is not a typed expression")
    ))()


def copy_typified(typed, new_typ):
    return match_typed(
        lazy_for_typed_lit=lambda: typed,
        lazy_for_typed_idf=lambda: TypedIdf(typed.s, new_typ),
        lazy_for_typed_call_1=(
            lambda: TypedCall1(typed.typed_f, typed.typed_x, new_typ)
        ),
        lazy_for_typed_lambda_1=(
            lambda: TypedLambda1(typed.t_idf_x, typed.typed_res, new_typ)
        ),
    )(typed)


def find_idf_type_call_1(self, s):
    lookup_by_f = find_idf_type(self.typed_f, s)
    return (lookup_by_f
            if not type(lookup_by_f) is Unknown0 else
            find_idf_type(self.typed_x, s)
            )


def find_idf_type(typed, s):
    return match_typed(
        lazy_for_typed_lit=lambda: T_A,
        lazy_for_typed_idf=lambda: typed.typ if typed.s == s else T_A,
        lazy_for_typed_call_1=lambda: find_idf_type_call_1(typed, s),
        lazy_for_typed_lambda_1=lambda: find_idf_type(typed.typed_res, s),
    )(typed)


def solve_rec(typ_sub_fx, typ_sub_x, f_x_synched_unks):
    typ_f, typ_x, synched_unks = f_x_synched_unks

    if type(typ_sub_fx) is Type0 and type(
            typ_sub_x) is Type0 and typ_sub_fx.s == typ_sub_x.s:
        return typ_f, typ_x, synched_unks

    if type(typ_sub_fx) is Type0 and type(typ_sub_x) is Unknown0:
        fail("Not implemented: solve_rec 1")

    if type(typ_sub_fx) is Unknown0 and type(typ_sub_x) is Type0:
        return solve(concrete(typ_f, typ_sub_fx, typ_sub_x), typ_x)

    if type(typ_sub_fx) is Unknown0:
        if type(typ_sub_x) is Unknown0:
            if typ_sub_fx.s == typ_sub_x.s:
                return typ_f, typ_x, synched_unks.union(
                    {typ_sub_fx.s})
            else:
                fail("Not implemented: solve_rec 4")
        else:
            if typ_sub_fx.s in f_x_synched_unks:
                return fail(
                    f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]"
                )

            return solve(concrete(typ_f, typ_sub_fx, typ_sub_x),
                typ_x)

    if type(typ_sub_fx) is Type1 and type(typ_sub_x) is Unknown0:
        if typ_sub_x.s in f_x_synched_unks:
            return fail(
                f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]")
        return fail(
            f"Yet can't call {typ_f} with {typ_x} currently matching {typ_sub_fx} and {typ_sub_x}"
        )

    if type(typ_sub_fx) is Type1 and type(typ_sub_x) is Type1:
        return solve_rec(typ_sub_fx.t1, typ_sub_x.t1,
            (typ_f, typ_x, synched_unks))

    if type(typ_sub_fx) is Type2 and type(typ_sub_x) is Unknown0:
        if typ_sub_x.s in f_x_synched_unks:
            return fail(
                f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]")
        return fail(
            f"Yet can't call {typ_f} with {typ_x} currently matching {typ_sub_fx} and {typ_sub_x}"
        )

    if type(typ_sub_fx) is Type2 and type(typ_sub_x) is Type2:
        return solve_rec(typ_sub_fx.t2, typ_sub_x.t2,
            solve_rec(typ_sub_fx.t1, typ_sub_x.t1,
                (typ_f, typ_x, synched_unks)))

    return fail(
        f"Can't match the types {typ_sub_fx} vs {typ_sub_x}")


def concreted(typ_f, typ_x):
    return solve(typ_f, typ_x)[0]


def solve(typ_f, typ_x):
    return solve_rec(typ_f.t1, typ_x, (typ_f, typ_x, set()))


def typify_x(typed_f, typed_x):
    new_typed_x = copy_typified(typed_x, typed_f.typ.t1)
    return TypedCall1(typed_f, new_typed_x, typed_f.typ.t2)


def typify_f(typed_f, typed_x):
    new_typ_f = concreted(typed_f.typ, typed_x.typ)
    new_typed_f = copy_typified(typed_f, new_typ_f)
    new_typed_x = copy_typified(typed_x, new_typ_f.t1)
    return TypedCall1(new_typed_f, new_typed_x, new_typ_f.t2)


def sem_expr_call_1(expr):
    typed_f = sem_rec(expr.expr_f)
    typed_x = sem_rec(expr.expr_x)

    fail_if(not (type(typed_f.typ) is Type2 and typed_f.typ.s == builtin_Func),
        f"typed_f should be a {builtin_Func}",
    )

    return (
        typify_x(typed_f, typed_x)
        if type(typed_x.typ) is Unknown0 else
        typify_f(typed_f, typed_x)
    )


def sem_expr_lambda_1(expr):
    t_idf_x = sem_rec(expr.expr_idf_arg)
    typed_res = sem_rec(expr.expr_res)

    lookup_typ_x = find_idf_type(typed_res, t_idf_x.s)

    retyped_x = copy_typified(t_idf_x, lookup_typ_x)

    return TypedLambda1(retyped_x, typed_res)


def sem_rec(expr):
    return match_expr(
        lazy_for_lit_str=lambda: TypedLit(expr.s, T_Str),
        lazy_for_idf=lambda: TypedIdf(expr.s,
            idf_to_type[expr.s] if expr.s in idf_to_type else T_A
        ),
        lazy_for_call_1=lambda: sem_expr_call_1(expr),
        lazy_for_lambda_1=lambda: sem_expr_lambda_1(expr),
    )(expr)


def sem(expr):
    typed = sem_rec(expr)
    fail_if(type(typed.typ) is not Type1 and typed.typ.s == builtin_RIO, "")
    fail_if(type(typed.typ) is Unknown0, "")
    return typed


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
