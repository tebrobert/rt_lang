from utils.flattap import *
from utils.rt_try import *


def rt_assert(cond, err_msg):
    assert cond, err_msg


def rt_assert_equal(label, expected_str):
    return lambda actual: (
        rt_assert(expected_str == str(actual), f"!={label}, got: {actual}")
    )


def rt_try_assert_equal(label, expected_str, lazy_actual):
    return flattap(lambda: rt_try(lazy_actual),
        rt_assert_equal(label, expected_str)
    )
