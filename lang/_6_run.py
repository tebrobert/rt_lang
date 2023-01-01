from lang._5_compile import *

class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

    def __repr__(self):
        return self.msg

def unsafeRunCompiled(rio):
    type_rio = type(rio)

    if type_rio is Input:
        return input()
    elif type_rio is Print:
        return print(rio.s)
    elif type_rio is Flatmap:
        return unsafeRunCompiled(rio.a_fb(unsafeRunCompiled(rio.fa)))
    else:
        raise RunErr(f'Unexpected type "{type_rio}" "{rio}".')
