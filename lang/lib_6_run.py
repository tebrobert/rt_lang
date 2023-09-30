from lang.lib_5_build import *


class RunErr(ValueError):
    def __init__(self, msg):
        self.msg = f"RunErr: {msg}"

    def __repr__(self):
        return self.msg

# stack unsafe
def unsafe_run_built(rio):
    return match_brick(
        lazy_for_input=lambda: input(),
        lazy_for_print=lambda: print(rio.s),
        lazy_for_flatmap=(
            lambda: unsafe_run_built(rio.a_fb(unsafe_run_built(rio.fa)))
        ),
        lazy_for_pure=lambda: rio.a,
    )(rio)
