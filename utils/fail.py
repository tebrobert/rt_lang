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


def rt_assert_equal(actual, expected):
    fail_if(str(expected) != str(actual),
        f"!=, expected: `{expected}`, got: `{actual}`"
    )
    return actual


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
