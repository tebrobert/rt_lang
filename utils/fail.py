import traceback
import inspect


# class RtError(Exception):
#     pass


def fail(*msgs):
    raise Exception(" ".join(msgs))
    # raise RtError(" ".join(msgs))


def fail_if(cond, *msg):
    if cond:
        fail(*msg)


def rt_assert(cond, msg="Assertion error."):
    fail_if(not cond, msg)


def wip(*msgs):
    caller_func_name = inspect.getouterframes(inspect.currentframe(), 2)[1][3]
    print("wip", caller_func_name)
    fail("The feature was not implemented. Work in progress...", *msgs)


def rt_assert_equal(actual, expected, label=""):
    rt_assert(str(expected) == str(actual),
        f"Not equal `{label}`: " +
        f"actual - `{actual}`, expected - `{expected}`.",
    )
    return actual


def rt_assert_type(value, expected_type):
    rt_assert_equal(type(value), expected_type)
    return value


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
