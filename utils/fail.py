def fail(msg):
    raise msg


def fail_if(cond, msg):
    if cond:
        fail(msg)
