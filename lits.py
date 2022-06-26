class LitError(ValueError):
    def __init__(self, msg):
        self.msg = msg


def is_type(o):
    return type(o) in [Unk_0, Type_0, Type_1, Type_2]


class Unk_0: #unknown
    def __init__(self, s):
        if not type(s) is str: raise LitError('The args of Unk_0 have bad types')
        self.s = s

    def __eq__(self, that):
        return (type(that) == Unk_0
            and that.s == self.s
        )
    
    def has_unk(self):
        return True
    
    def concrete(self, typ_from, typ_to):
        return typ_to if self == typ_from else self
    
    def __repr__(self, indent=''):
        return (
            #f'{indent}Unk_0("{self.s}")'
            #self.s
            f'{indent}{self.s}'
        )


class Type_0:
    def __init__(self, s):
        if not type(s) is str: raise LitError('The args of Type_0 have bad types')
        self.s = s

    def __eq__(self, that):
        return (type(that) == Type_0
            and that.s == self.s
        )
    
    def has_unk(self):
        return False
        
    def concrete(self, typ_from, typ_to):
        return self
        
    def __repr__(self, indent=''):
        return (
            #f'{indent}Type_0("{self.s}")'
            #self.s
            f'{indent}{self.s}'
        )


class Type_1:
    def __init__(self, s, t1):
        if not (type(s) is str and is_type(t1)): raise LitError('The args of Type_1 have bad types')
        self.s = s
        self.t1 = t1
    
    def __eq__(self, that):
        return (type(that) == Type_1
            and that.s == self.s
            and that.t1 == self.t1
        )
    
    def __repr__(self, indent=''):
        return (
            #f'{indent}Type_2("{self.s}", {self.t1})'
            #f'{self.s}[{self.t1}]'
            f'{indent}{self.s}[{self.t1}]'
        )
    
    def copy(self, t1):
        return Type_1(self.s, t1)

    def has_unk(self):
        return self.t1.has_unk()
    
    def concrete(self, typ_from, typ_to):
        return Type_1(self.s, self.t1.concrete(typ_from, typ_to))


class Type_2:
    def __init__(self, s, t1, t2):
        if not (type(s) is str and is_type(t1) and is_type(t2)):
            print('\ndev type2')
            print('dev', s)
            print('dev', t1)
            print('dev', t2)
            raise LitError('The args of Type_2 have bad types')
        self.s = s
        self.t1 = t1
        self.t2 = t2
    
    def __eq__(self, that):
        return (type(that) == Type_2
            and that.s == self.s
            and that.t1 == self.t1
            and that.t2 == self.t2
        )

    def __repr__(self, indent=''):
        return indent + (
            f'({self.t1}) => {self.t2}'
            if self.s == 'Func' and self.t1.s == 'Func' else
        
            f'{self.t1} => {self.t2}'
            if self.s == 'Func' else
        
            #f'{indent}Type_2("{self.s}", {self.t1}, {self.t2})'
            f'{self.s}[{self.t1}, {self.t2}]'
        )

    def copy(self, t1=None, t2=None):
        t1_ = t1 if t1 is not None else self.t1
        t2_ = t2 if t2 is not None else self.t2
        return Type_2(self.s, t1_, t2_)

    def has_unk(self):
        return self.t1.has_unk() or self.t2.has_unk()

    def concrete(self, typ_from, typ_to):
        return Type_2(self.s, self.t1.concrete(typ_from, typ_to), self.t2.concrete(typ_from, typ_to))


T_Int = Type_0('Int')
T_Str = Type_0('Str')
T_Unit = Type_0('Unit')
T_List = lambda t1: Type_1('List', t1)
T_RIO = lambda t1: Type_1('RIO', t1)
T_Func = lambda t1, t2: Type_2('Func', t1, t2)

T_A = Unk_0('A')
T_B = Unk_0('B')


types = {
    'Int': T_Int,
    'Str': T_Str,
    'Unit': T_Unit,
    'List': T_List,
    'RIO': T_RIO,
    'Func': T_Func,
}

idf_to_type = {
    'input': T_RIO(T_Str),
    'print': T_Func(T_Str, T_RIO(T_Unit)),
    'flatmap': T_Func(T_Func(T_A, T_RIO(T_B)), T_Func(T_RIO(T_A), T_RIO(T_B))),
}