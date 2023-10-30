from lang.lib_2_tokenize import *
from utils.list_match import *


class ExprLitStr:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f"""{indent}Expr_Lit_Str("{self.s}")"""

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class ExprIdf:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f"""{indent}Expr_Idf("{self.s}")"""

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class ExprCall1:
    def __init__(self, expr_f, expr_x):
        self.expr_f, self.expr_x = expr_f, expr_x

    def __repr__(self, indent=''):
        return (f"{indent}Expr_Call_1(\n"
                + f"""{self.expr_f.__repr__(indent + 4 * " ")},\n"""
                + f"""{self.expr_x.__repr__(indent + 4 * " ")}\n"""
                + f"""{indent})"""
                )

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class ExprLambda1:
    def __init__(self, expr_idf_arg, expr_res):
        fail_if(type(expr_idf_arg) is not ExprIdf,
            "Expr_Idf expected as the first arg of Expr_Lambda_1",
        )

        self.expr_idf_arg, self.expr_res = expr_idf_arg, expr_res

    def __repr__(self, indent=''):
        return (f"{indent}Expr_Lambda_1(\n"
                + f"""{self.expr_idf_arg.__repr__(indent + 4 * " ")},\n"""
                + f"""{self.expr_res.__repr__(indent + 4 * " ")}\n"""
                + f"""{indent})"""
                )

    def __eq__(self, that):
        return f"{self}" == f"{that}"


def match_expr(
    case_lit_str,
    case_idf,
    case_call_1,
    case_lambda_1,
):
    return lambda typed: ({
        ExprLitStr: lambda: case_lit_str(typed.s),
        ExprIdf: lambda: case_idf(typed.s),
        ExprCall1: lambda: case_call_1(typed.expr_f, typed.expr_x),
        ExprLambda1: lambda: case_lambda_1(typed.expr_idf_arg, typed.expr_res),
    }
    .get(
        type(typed),
        lambda: fail(f"Value {typed} {type(typed)} is not an Expr.")
    ))()


@tailrec
def parse_first_of(ext_tokens, current_idx, parsers):
    def try_next_parser(current_parser, rest_parsers):
        either_result = rt_try(lambda: current_parser(ext_tokens, current_idx))
        return (
            rec(ext_tokens, current_idx, rest_parsers)
            if is_fail(either_result) else
            either_result
        )

    return match_list(
        case_empty=lambda: fail(
            f"Can't parse Expr given {current_idx} {ext_tokens}."
        ),
        case_nonempty=lambda head, tail: try_next_parser(head, tail),
    )(parsers)


def parse_lit_str(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenLitStr,
        f"TokenLitStr expected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return ExprLitStr(ext_tokens[current_idx].s), current_idx + 1


def parse_idf(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenIdf,
        f"TokenIdf expected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return ExprIdf(ext_tokens[current_idx].s), current_idx + 1


def parse_braced_full_expr(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenParenOpen,
        f"TokenParenOpen expected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    expr, paren_close_idx = parse_full_expr(ext_tokens, current_idx + 1)
    fail_if(type(ext_tokens[paren_close_idx]) is not TokenParenClose,
        f"TokenParenClose expected at {paren_close_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return expr, paren_close_idx + 1


def parse_lambda_1(ext_tokens, current_idx):
    e_idf_x, eq_gr_idx = parse_idf(ext_tokens, current_idx)
    fail_if(type(ext_tokens[eq_gr_idx]) is not TokenEqGr,
        f"TokenEqGr expected at {eq_gr_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    expr_res, next_idx = parse_full_expr(ext_tokens, eq_gr_idx + 1)
    return ExprLambda1(e_idf_x, expr_res), next_idx


def parse_atomic_expr(ext_tokens, current_idx):
    return parse_first_of(ext_tokens, current_idx, [
        parse_lambda_1,
        parse_idf,
        parse_braced_full_expr,
        parse_lit_str,
    ])


@tailrec
def continue_parsing_call_rec(ext_tokens, expr_f, current_idx):
    def continue_parsing_call_forced():
        parsed_braced_expr, post_braced_idx = parse_braced_full_expr(
            ext_tokens, current_idx
        )
        parsed_expr = ExprCall1(expr_f, parsed_braced_expr)
        return rec(ext_tokens, parsed_expr, post_braced_idx)

    return (
        continue_parsing_call_forced()
        if type(ext_tokens[current_idx]) is TokenParenOpen else
        (expr_f, current_idx)
    )


def parse_call_expr(ext_tokens, current_idx):
    parsed_atomic_expr, post_atomic_idx = parse_atomic_expr(
        ext_tokens, current_idx
    )
    return continue_parsing_call_rec(
        ext_tokens, parsed_atomic_expr, post_atomic_idx
    )


@tailrec
def continue_parsing_dotting_rec(ext_tokens, expr_acceptor, current_idx):
    def continue_parsing_dotting_forced():
        parsed_expr_method, post_braced_idx = parse_call_expr(
            ext_tokens, current_idx + 1
        )
        parsed_expr = ExprCall1(parsed_expr_method, expr_acceptor)
        return rec(ext_tokens, parsed_expr, post_braced_idx)

    return (
        continue_parsing_dotting_forced()
        if type(ext_tokens[current_idx]) is TokenDot else
        (expr_acceptor, current_idx)
    )


def parse_full_expr(ext_tokens, current_idx):
    parsed_call_expr, post_call_idx = parse_call_expr(ext_tokens, current_idx)
    return continue_parsing_dotting_rec(
        ext_tokens, parsed_call_expr, post_call_idx
    )


def parse(tokens):
    end_of_tokens = "\0"
    ext_tokens = tokens + [end_of_tokens]
    expr, current_idx = parse_full_expr(ext_tokens, 0)

    fail_if(ext_tokens[current_idx] != end_of_tokens,
        f"Unexpected token at {current_idx} given {tokens}",
    )

    return expr


def full_parse(code):
    return parse(full_tokenize(code))


def full_parse2(code):
    return parse(full_tokenize2(code))
