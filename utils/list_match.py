def list_match(lazy_for_empty, lazy_for_nonempty):
    def list_matcher(list):
        return (lazy_for_empty()
                if list == [] else
                lazy_for_nonempty(list[0], list[1:])
                )

    return list_matcher
