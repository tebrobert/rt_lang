from utils.flattap import flattap


def fail(*msgs):
    raise Exception(" ".join(msgs))


def fail_if(cond, *msg):
    if cond:
        fail(*msg)


def rt_assert(cond):
    fail_if(not cond, "Assertion error.")


def rt_assert_equal(label, expected_str):
    return lambda actual: (
        fail_if(expected_str != str(actual),
            f"!={label}, expected: `{expected_str}`, got: `{actual}`"
        )
    )


def rt_try_assert_equal(label, expected_str, lazy_actual):
    return flattap(lambda: rt_try(lazy_actual),
        rt_assert_equal(label, expected_str)
    )


def rt_try(action):
    try:
        return action()
    except Exception as e:
        return e


def is_fail(v):
    return isinstance(v, Exception)
