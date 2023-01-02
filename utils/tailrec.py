class RecExc(Exception):
    def __init__(self, *args, **kwargs):
        self.args, self.kwargs = args, kwargs

def tailrec(f):
    def dec(*args, **kwargs):
        while True:
            try:
                return f(*args, **kwargs)

            except RecExc as re:
                args, kwargs = re.args, re.kwargs

    return dec

def rec(*args, **kwargs):
    raise RecExc(*args, **kwargs)
