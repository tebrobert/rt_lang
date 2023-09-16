def print_if(cond):
    def print_if_cond(value, end="\n"):
        if cond:
            print(value, end=end)

    return print_if_cond


def print_with_header_if(cond):
    def mb_print_with_header(header):
        def mb_print_headered(value):
            print_if(cond)(f"{header}:")
            print_if(cond)(f"{value}\n")

        return mb_print_headered

    return mb_print_with_header
