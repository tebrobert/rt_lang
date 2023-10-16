from lang.lib_2_lexx import *


class ExprLitStr:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f"""{indent}Expr_Lit_Str("{self.s}")"""


class ExprIdf:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f"""{indent}Expr_Idf("{self.s}")"""


class ExprCall1:
    def __init__(self, expr_f, expr_x):
        self.expr_f, self.expr_x = expr_f, expr_x

    def __repr__(self, indent=''):
        return (f"{indent}Expr_Call_1(\n"
                + f"""{self.expr_f.__repr__(indent + 4 * " ")},\n"""
                + f"""{self.expr_x.__repr__(indent + 4 * " ")}\n"""
                + f"""{indent})"""
                )


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


def match_expr(
    lazy_for_lit_str,
    lazy_for_idf,
    lazy_for_call_1,
    lazy_for_lambda_1,
):
    return lambda typed: ({
        ExprLitStr: lazy_for_lit_str,
        ExprIdf: lazy_for_idf,
        ExprCall1: lazy_for_call_1,
        ExprLambda1: lazy_for_lambda_1,
    }
    .get(
        type(typed),
        lambda: fail(f"Value {typed} {type(typed)} is not an Expr.")
    ))()


@tailrec
def parse_first_of(ext_tokens, current_idx, parsers):
    fail_if(parsers == [],
        f"Can't parse Expr given {current_idx} {ext_tokens}."
    )
    either_result = rt_try(lambda: parsers[0](ext_tokens, current_idx))
    return (
        rec(ext_tokens, current_idx, parsers[1:])
        if is_fail(either_result) else
        either_result
    )


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


def parse_braced(ext_tokens, current_idx):
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
        parse_braced,
        parse_lit_str,
    ])


@tailrec
def continue_parsing_call(ext_tokens, expr_f, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenParenOpen,
        f"TokenParenOpen: expected. Given `{current_idx}` `{ext_tokens}`",
    )
    fail_if(type(ext_tokens[current_idx + 1]) is TokenParenClose,
        f"Remove deprecated empty parenthesis.",
        f"Given `{current_idx}` `{ext_tokens}`",
    )

    expr_x, paren_close_idx = parse_full_expr(ext_tokens, current_idx + 1)
    fail_if(type(ext_tokens[paren_close_idx]) is not TokenParenClose,
        f"TokenParenOpen expected at `{paren_close_idx}`.",
        f"Given {current_idx} {ext_tokens}",
    )

    next_idx = paren_close_idx + 1
    parsed_expr = ExprCall1(expr_f, expr_x)
    return (rec(ext_tokens, parsed_expr, next_idx)
            if type(ext_tokens[next_idx]) is TokenParenOpen else
            (parsed_expr, next_idx)
            )


def parse_full_expr(ext_tokens, current_idx):
    parsed_expr, next_idx = parse_atomic_expr(ext_tokens, current_idx)
    return (continue_parsing_call(ext_tokens, parsed_expr, next_idx)
            if type(ext_tokens[next_idx]) is TokenParenOpen else
            (parsed_expr, next_idx)
            )


def parse(tokens):
    end_of_tokens = "\0"
    ext_tokens = tokens + [end_of_tokens]
    expr, current_idx = parse_full_expr(ext_tokens, 0)

    fail_if(ext_tokens[current_idx] != end_of_tokens,
        f"Unexpected token at {current_idx} given {tokens}",
    )

    return expr
