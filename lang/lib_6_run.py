from lang.lib_5_build import *


class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

    def __repr__(self):
        return self.msg


def unsafe_run_built(rio):
    type_rio = type(rio)

    if type_rio is BrickInput:
        return input()
    elif type_rio is BrickPrint:
        return print(rio.s)
    elif type_rio is BrickFlatmap:
        return unsafe_run_built(rio.a_fb(unsafe_run_built(rio.fa)))
    elif type_rio is BrickPure:
        return rio.a
    else:
        return fail(RunErr(f"""Unexpected type "{type_rio}" "{rio}"."""))
