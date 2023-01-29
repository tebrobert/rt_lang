from utils.flattap import *
from utils.TRY import *

def ASSERT(expected, actual, errMsg):
    assert expected == actual, errMsg

def ASSERT_TEST(label, expectedStr):
    def partiallyApplied(actual):
        assert expectedStr == str(actual), f"!={label}, got: {actual}"
    return partiallyApplied

def TRY_ASSERT_TEST(label, expectedStr, lazyActual):
    return flattap(
        lambda: TRY(lazyActual),
        ASSERT_TEST(label, expectedStr)
    )