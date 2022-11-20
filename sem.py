from parse import *
from lits import *

class SemErr(ValueError):
    def __init__(self, msg): self.msg = f"SemErr: {msg}"
    def __repr__(self): return self.msg

class Typed_Lit:
    def __init__(self, s, typ): self.s, self.typ = s, typ
    def __repr__(self, indent=''): return f'{indent}Typed_Lit("{self.s}", {self.typ})'
    def copy_typified(self, new_typ): return self
    def find_idf_type(self, s): return T_A

class Typed_Idf:
    def __init__(self, s, typ): self.s, self.typ = s, typ
    def __repr__(self, indent=''): return f'{indent}Typed_Idf("{self.s}", {self.typ})'
    def copy_typified(self, new_typ): return Typed_Idf(self.s, new_typ)
    def find_idf_type(self, s): return self.typ if self.s == s else T_A
class Typed_Call_1:
    def __init__(self, typed_f, typed_x, typ): self.typed_f, self.typed_x, self.typ = typed_f, typed_x, typ
    def __repr__(self, indent=''):
        shift = indent + 4*' '
        return f'{indent}Typed_Call_1(\n{self.typed_f.__repr__(shift)},\n{self.typed_x.__repr__(shift)},\n{self.typ.__repr__(shift)}\n{indent})'
    def copy_typified(self, new_typ): return Typed_Call_1(self.typed_f, self.typed_x, new_typ)
    def find_idf_type(self, s):
        lookup_by_f = self.typed_f.find_idf_type(s)
        return lookup_by_f if not type(lookup_by_f) is Unk_0 else self.typed_x.find_idf_type(s)
class Typed_Lambda_1:
    def __init__(self, tidf_x, typed_res, typ=None):
        if not (type(tidf_x) is Typed_Idf): raise SemErr('Typed_Idf expected as the first arg of Typed_Lambda_1')
        if not (typ is None or type(typ) is Type_2 and typ.s == builtin_Func): raise SemErr(f'{builtin_Func} or None expected as the typ arg of Typed_Lambda_1')
        self.tidf_x = tidf_x
        self.typed_res = typed_res
        self.typ = typ if typ is not None else T_Func(tidf_x.typ, typed_res.typ)
    def __repr__(self, indent=''):
        shift = indent + 4*' '
        return f'{indent}Typed_Lambda_1(\n{self.tidf_x.__repr__(shift)},\n{self.typed_res.__repr__(shift)},\n{self.typ.__repr__(shift)}\n{indent})'
    def copy_typified(self, new_typ): return Typed_Lambda_1(self.tidf_x, self.typed_res, new_typ)
    def find_idf_type(self, s): return self.typed_res.find_idf_type(s)
def sem_rec(expr):
    type_expr = type(expr)
    if type_expr is Expr_Lit_Str: return Typed_Lit(expr.s, T_Str)
    if type_expr is Expr_Idf: return Typed_Idf(expr.s, idf_to_type[expr.s] if expr.s in idf_to_type else T_A)
    
    if type_expr is Expr_Call_1:
        typed_f = sem_rec(expr.expr_f)
        typed_x = sem_rec(expr.expr_x)
        
        if type(typed_f.typ) is Type_2 and typed_f.typ.s == builtin_Func:
            if type(typed_x.typ) is Unk_0:
                new_typed_x = typed_x.copy_typified(typed_f.typ.t1)
                return Typed_Call_1(typed_f, new_typed_x, typed_f.typ.t2)
            
            def concreted(typ_f, typ_x):
                def solve(typ_f, typ_x): return solve_rec(typ_f.t1, typ_x, (typ_f, typ_x, set()))

                def solve_rec(typ_sub_fx, typ_sub_x, f_x_synchedUnks):
                    typ_f, typ_x, synched_unks = f_x_synchedUnks
                    
                    if type(typ_sub_fx) is Type_0 and type(typ_sub_x) is Type_0 and typ_sub_fx.s == typ_sub_x.s: return (typ_f, typ_x, synched_unks)
                    if type(typ_sub_fx) is Type_0 and type(typ_sub_x) is Unk_0: wip('solve_rec 1')
                    if type(typ_sub_fx) is Unk_0 and type(typ_sub_x) is Type_0: return solve(typ_f.concrete(typ_sub_fx, typ_sub_x), typ_x)
                    
                    if type(typ_sub_fx) is Unk_0:
                        if type(typ_sub_x) is Unk_0:
                            if typ_sub_fx.s == typ_sub_x.s:
                                return (typ_f, typ_x, synched_unks.union({typ_sub_fx.s}))
                            else: wip('solve_rec 4')
                        else:
                            if typ_sub_fx.s in f_x_synchedUnks:
                                raise SemErr(f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]")
                            #raise SemErr(f"Yet can't call {typ_f} with {typ_x} currentrly matching {typ_sub_fx} and {typ_sub_x}")
                            return solve(typ_f.concrete(typ_sub_fx, typ_sub_x), typ_x)
                    
                    if type(typ_sub_fx) is Type_1 and type(typ_sub_x) is Unk_0:
                        if typ_sub_x.s in f_x_synchedUnks: raise SemErr(f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]")
                        raise SemErr(f"Yet can't call {typ_f} with {typ_x} currentrly matching {typ_sub_fx} and {typ_sub_x}")
                    
                    if type(typ_sub_fx) is Type_1 and type(typ_sub_x) is Type_1: return solve_rec(typ_sub_fx.t1, typ_sub_x.t1, (typ_f, typ_x, synched_unks))
                    
                    if type(typ_sub_fx) is Type_2 and type(typ_sub_x) is Unk_0:
                        if typ_sub_x.s in f_x_synchedUnks: raise SemErr(f"Can't match the types #remember the case A=>A vs A=>{builtin_List}[A]")
                        raise SemErr(f"Yet can't call {typ_f} with {typ_x} currentrly matching {typ_sub_fx} and {typ_sub_x}")
                    
                    if type(typ_sub_fx) is Type_2 and type(typ_sub_x) is Type_2: return solve_rec(typ_sub_fx.t2, typ_sub_x.t2, solve_rec(typ_sub_fx.t1, typ_sub_x.t1, (typ_f, typ_x, synched_unks)))
                    
                    raise SemErr(f"Can't match the types {typ_sub_fx} vs {typ_sub_x}")
                
                return solve(typ_f, typ_x)[0]
            
            new_typ_f = concreted(typed_f.typ, typed_x.typ)
            new_typed_f = typed_f.copy_typified(new_typ_f)
            new_typed_x = typed_x.copy_typified(new_typ_f.t1)
            return Typed_Call_1(new_typed_f, new_typed_x, new_typ_f.t2)
            
        raise SemErr(f'typed_f should be a {builtin_Func}')
    
    if type_expr is Expr_Lambda_1:
        tidf_x = sem_rec(expr.eidf_x)
        typed_res = sem_rec(expr.expr_res)
        
        lookup_typ_x = typed_res.find_idf_type(tidf_x.s)
        
        retyped_x = tidf_x.copy_typified(lookup_typ_x)
        
        return Typed_Lambda_1(retyped_x, typed_res)
        
    raise SemErr(f'expr has unexpected type {type_expr}')

def sem(expr):
    typed = sem_rec(expr)
    if not (type(typed.typ) is Type_1 and typed.typ.s == builtin_RIO):
        if type(typed.typ) is Unk_0:
                unk = typed
                raise SemErr(f"Unexpected identifier {unk.s}")
        raise SemErr(f"The code type should be {builtin_RIO}[A] but {typed.typ} found.")
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
