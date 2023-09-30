from utils.flattap import *
from utils.rt_try import *


def fail(msg):
    raise msg


def fail_if(cond, msg):
    if cond:
        fail(msg)


def rt_assert(cond):
    fail_if(not cond, "Assertion error.")


def rt_assert_equal(label, expected_str):
    return lambda actual: (
        fail_if(expected_str != str(actual), f"!={label}, got: {actual}")
    )


def rt_try_assert_equal(label, expected_str, lazy_actual):
    return flattap(lambda: rt_try(lazy_actual),
        rt_assert_equal(label, expected_str)
    )
