from lang.lib_5_build import *


# stack unsafe
def unsafe_run_built(rio):
    return match_brick(
        case_input=lambda: input(),
        case_print=lambda s: print(s),
        case_flatmap=(
            lambda a_fb, fa: unsafe_run_built(a_fb(unsafe_run_built(fa)))
        ),
        case_pure=lambda a: a,
    )(rio)
