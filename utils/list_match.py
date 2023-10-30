def match_list(case_empty, case_nonempty):
    return lambda vals: (
        case_empty()
        if len(vals) == 0 else
        case_nonempty(vals[0], vals[1:])
    )
