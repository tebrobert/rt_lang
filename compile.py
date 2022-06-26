from sem import *

class CompileErr(ValueError):
    def __init__(self, msg):
        self.msg = f"CompileErr: {msg}"

'''
class Arg:
    def __repr__(self):
        return f'Arg'
    def runnable(self): return False
'''

class CallableShowableLambda_1:
    def __init__(self, f, s):
        if not callable(f):
            raise ValueError("f should be callable")
        if not type(s) is str:
            raise ValueError("s should be a string")
        self.s = s
        self.f = f
    def __call__(self, arg):
        return self.f(arg)
    def __repr__(self):
        return self.s
    def runnable(self): return False
    
class Input:
    def __repr__(self):
        return f'Input'
    def runnable(self): return True

class Print:
    def __init__(self, s):
        self.s = s
    def __repr__(self):
        return f'Print("{self.s}")'
    def runnable(self): return True

class Flatmap:
    def __init__(self, a_fb, fa):
        if not callable(a_fb):
            raise ValueError("a_fb should be callable")
        if not fa.runnable():
            raise ValueError(f"fa should be runnable but it is {fa=}, also {a_fb=}")
        self.a_fb = a_fb
        self.fa = fa
    def __repr__(self):
        if type(self.a_fb) is CallableShowableLambda_1:
            return f'Flatmap({self.a_fb}, {self.fa})'
        return f'Flatmap(lambda ..., {self.fa})'
    def runnable(self): return True

'''
def compile_v1(typed, lamb_arg_stack=[]):
    def dev(*args):
        #return
        list(map(lambda s: print(f'dev {s}'), args))
        print('\n')

    if type(typed) is Typed_Idf:
        tidf = typed
        if tidf.s == 'input':
            return Input()
        if tidf.s == 'print':
            return CallableShowableLambda_1(lambda s: Print(s), 'lambda s: Print(s)')
        if tidf.s == 'flatmap':
            return CallableShowableLambda_1(lambda a_fb: CallableShowableLambda_1(lambda fa: Flatmap(a_fb, fa), f'lambda fa: Flatmap({a_fb}, fa)'), 'lambda a_fb: lambda fa: Flatmap(a_fb, fa)')
       #if lamb_arg_stack is not [] and tidf.s == lamb_arg_stack[0]:
        if lamb_arg_stack is not [] and tidf.s in lamb_arg_stack:
            return Arg()
        raise CompileErr(f"Unexpected identifier {tidf}")

    if type(typed) is Typed_Call_1:
        tcall = typed
        # NO IDEA HOW TO DO IT RIGHT
        compiled_f = compile(tcall.typed_f, lamb_arg_stack=lamb_arg_stack)
        compiled_x = compile(tcall.typed_x, lamb_arg_stack=lamb_arg_stack)
        if callable(compiled_f) and type(compiled_x) is not Arg:
            dev('br 1', tcall, compiled_f, compiled_x)
            return compiled_f(compiled_x)
        if callable(compiled_f) and type(compiled_x) is Arg:
            return compiled_f
        raise CompileErr(f"Can't compile {typed}")
        
    
    if type(typed) is Typed_Lambda_1:
        tlamb = typed
        return compile(tlamb.typed_res, [tlamb.tidf_x.s]+lamb_arg_stack)
    
    raise CompileErr(f"Unexpected typed expression {typed}")
'''

def show(typed, lamb_arg_stack=[]):
    if type(typed) is Typed_Idf:
        tidf = typed

        if tidf.s == "input":
            return "Input()"
        if tidf.s == "print":
            return "(lambda s: Print(s))"
        if tidf.s == "flatmap":
            return "(lambda a_fb: lambda fa: Flatmap(a_fb, fa))"
        if tidf.s in lamb_arg_stack:
            return tidf.s
        raise CompileErr(f"Unexpected identifier `{tidf.s}`")

    if type(typed) is Typed_Call_1:
        tcall = typed
        shown_f = show(tcall.typed_f, lamb_arg_stack)
        shown_x = show(tcall.typed_x, lamb_arg_stack)
        return f"({shown_f})({shown_x})"
    
    if type(typed) is Typed_Lambda_1:
        tlamb = typed
        s = tlamb.tidf_x.s
        return f"(lambda {s}: {show(tlamb.typed_res, [s]+lamb_arg_stack)})"
    
    raise CompileErr(f"Unexpected typed expression {typed}")

def compile(shown):
    return eval(shown)
