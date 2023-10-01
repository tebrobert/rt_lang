from lang.lib_5_build import *


# stack unsafe
def unsafe_run_built(rio):
    return match_brick(
        lazy_for_lit_str=lambda: input(),
        lazy_for_idf=lambda: print(rio.s),
        lazy_for_call_1=(
            lambda: unsafe_run_built(rio.a_fb(unsafe_run_built(rio.fa)))
        ),
        lazy_for_lambda_1=lambda: rio.a,
    )(rio)
