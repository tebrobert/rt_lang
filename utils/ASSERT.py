from utils.flattap import *
from utils.TRY import *

def ASSERT(label, expectedStr):
    def partiallyApplied(actual):
        assert str(actual) == expectedStr, f"!={label}, got: {actual}"
    return partiallyApplied

def TRY_ASSERT(label, expectedStr, lazyActual):
    return flattap(
        lambda: TRY(lazyActual),
        ASSERT(label, expectedStr)
    )