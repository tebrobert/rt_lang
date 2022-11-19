from compile import *

class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

def unsafe_run(rio):
    type_rio = type(rio)

    if type_rio is Input:
        return input()

    elif type_rio is Print:
        return print(rio.s)

    elif type_rio is Flatmap:
        return unsafe_run(rio.a_fb(unsafe_run(rio.fa)))

    else:
        raise RunErr(f'Unexpected type "{type_rio}" "{rio}".')
