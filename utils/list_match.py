from utils.fail import *


def match_list(
    case_empty=None,
    case_at_least_1=None,
    case_at_least_2=None,
    otherwise=None,
):
    params = (case_empty, case_at_least_1, case_at_least_2, otherwise)
    return {
        (True, True, False, False): lambda: _match_list_01(
            case_empty=case_empty,
            case_at_least_1=case_at_least_1,
        ),
        (True, True, True, False): lambda: wip(),
        (True, False, False, True): lambda: wip(),
        (True, False, False, True): lambda: wip(),
        (False, True, False, True): lambda: wip(),
        (False, True, True, True): lambda: wip(),
        (False, False, True, True): lambda: lambda: _match_list_2o(
            case_at_least_2=case_at_least_2,
            otherwise=otherwise,
        ),
    }.get(
        tuple(map(bool, params)),
        lambda: fail("Wrong parameters."),
    )()


def _match_list_01(case_empty, case_at_least_1):
    return lambda vals: (
        case_empty()
        if len(vals) == 0 else
        case_at_least_1(vals[0], vals[1:])
    )


def _match_list_2o(case_at_least_2, otherwise):
    return _match_list_01(
        case_empty=otherwise,
        case_at_least_1=lambda head1, tail1: (
            _match_list_01(
                case_empty=otherwise,
                case_at_least_1=lambda head2, tail2: case_at_least_2(
                    head1, head2, tail2
                ),
            )(tail1)
        )
    )
