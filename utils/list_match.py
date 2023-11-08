from utils.fail import *


def match_list(
    case_empty=None,
    case_at_least_1=None,
    case_at_least_2=None,
    case_at_least_3=None,
    otherwise=None,
):
    params = (case_empty, case_at_least_1, case_at_least_2, case_at_least_3,
        otherwise,
    )
    return {
        (True, True, False, False, False): lambda: _match_list_01(
            case_empty=case_empty,
            case_at_least_1=case_at_least_1,
        ),
        (False, False, True, False, True): lambda: lambda: _match_list_2o(
            case_at_least_2=case_at_least_2,
            otherwise=otherwise,
        ),
        (True, True, True, True, False): lambda:
            _match_list_0123(
                case_empty=case_empty,
                case_at_least_1=case_at_least_1,
                case_at_least_2=case_at_least_2,
                case_at_least_3=case_at_least_3,
            ),
    }.get(
        tuple(map(bool, params)),
        lambda: fail("Wrong or not implemented parameters."),
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


def _match_list_0123(case_empty, case_at_least_1, case_at_least_2,
    case_at_least_3,
):
    return _match_list_01(
        case_empty=lambda: case_empty(),
        case_at_least_1=lambda head0, tail0: _match_list_01(
            case_empty=lambda: case_at_least_1(head0, []),
            case_at_least_1=lambda head1, tail1: _match_list_01(
                case_empty=lambda: case_at_least_2(head0, head1, []),
                case_at_least_1=lambda head2, tail2: case_at_least_3(
                    head0, head1, head2, tail2,
                ),
            )(tail1),
        )(tail0),
    )


def rt_assert_empty(vals, *msg):
    return _match_list_01(
        case_at_least_1=lambda _head, _tail: fail(
            f"The list is not empty, `{vals}`.",
            *msg,
        ),
        case_empty=lambda: None,
    )(vals)


def rt_assert_at_least_1(vals, *msg):
    return _match_list_01(
        case_at_least_1=lambda head, tail: (head, tail),
        case_empty=lambda: fail(*msg),
    )(vals)


def rt_assert_at_least_2(vals, *msg):
    return _match_list_2o(
        case_at_least_2=lambda head0, head1, tail: (
            head0, head1, tail,
        ),
        otherwise=fail(*msg),
    )(vals)
