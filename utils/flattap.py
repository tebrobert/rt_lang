def flattap(action, result_processor):
    res = action()
    result_processor(res)
    return res
