def flattap(action, resultProcessor):
    res = action()
    resultProcessor(res)
    return res