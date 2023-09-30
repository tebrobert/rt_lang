from lang.lib_5_build import *


class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

    def __repr__(self):
        return self.msg


class Input:
    def __repr__(self):
        return 'Input()'


class Print:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Print("{self.s}")'


class Flatmap:
    def __init__(self, a_fb, fa):
        fail_if(not type(a_fb) is CallableShowableLambda1,
            "a_fb should be CallableShowableLambda1",
        )
        self.a_fb, self.fa = a_fb, fa

    def __repr__(self):
        return f'Flatmap({self.a_fb}, {self.fa})'


class Pure:
    def __init__(self, a):
        self.a = a

    def __repr__(self):
        return f"Pure({self.a})"


def unsafe_run_built(rio):
    type_rio = type(rio)

    if type_rio is Input:
        return input()
    elif type_rio is Print:
        return print(rio.s)
    elif type_rio is Flatmap:
        return unsafe_run_built(rio.a_fb(unsafe_run_built(rio.fa)))
    elif type_rio is Pure:
        return rio.a
    else:
        return fail(RunErr(f"""Unexpected type "{type_rio}" "{rio}"."""))
