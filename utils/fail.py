from utils.flattap import flattap
import traceback


# class RtError(Exception):
#     pass


def fail(*msgs):
    raise Exception(" ".join(msgs))
    # raise RtError(" ".join(msgs))


def fail_if(cond, *msg):
    if cond:
        fail(*msg)


def rt_assert(cond):
    fail_if(not cond, "Assertion error.")


def wip():
    fail("The feature was not implemented. Work in progress...")


def rt_assert_equal(label, actual):
    def f(expected):
        fail_if(str(expected) != str(actual),
            f"!={label}, expected: `{expected}`, got: `{actual}`"
        )
        return actual

    return f


def rt_try_assert_equal(label, expected_str, lazy_actual):
    return flattap(lambda: rt_try(lazy_actual),
        rt_assert_equal(label, expected_str)
    )


def rt_try(action):
    try:
        return action()
    # except RtError as rt_error:
    #    return rt_error
    except:
        return Exception(traceback.format_exc())
        # return RtError(traceback.format_exc())


def is_fail(v):
    return isinstance(v, Exception)


def is_success(v):
    return not is_fail(v)
