from utils.flattap import *
from utils.TRY import *

def ASSERT(cond, errMsg):
    assert cond, errMsg

def ASSERT_EQUAL(label, expectedStr):
    return lambda actual: \
        ASSERT(expectedStr == str(actual), f"!={label}, got: {actual}")

def TRY_ASSERT_EQUAL(label, expectedStr, lazyActual):
    return flattap(
        lambda: TRY(lazyActual),
        ASSERT_EQUAL(label, expectedStr)
    )