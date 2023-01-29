from utils.flattap import *
from utils.TRY import *

def ASSERT(label, expected):
    def partiallyApplied(actual):
        assert actual == expected, f"!={label}, got: {actual}"
    return partiallyApplied

def TRY_ASSERT(label, expected, action, value):
    return flattap(
        lambda: TRY(lambda: action(value)),
        ASSERT(label, expected)
    )