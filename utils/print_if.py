def print_if(cond):
    def print_if_cond(value, end="\n"):
        if cond:
            print(value, end=end)

    return print_if_cond
