def match_list(case_empty, case_nonempty):
    return lambda vals: (
        case_empty()
        if len(vals) == 0 else
        case_nonempty(vals[0], vals[1:])
    )

# case_at_most_0    +   +   +
# case_at_least_1   +   +
# case_at_least_2       +           +
# case otherwise            +       +