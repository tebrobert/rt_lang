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


def try_parse_lit_str(tokens, i):
    fail_if(type(tokens[i]) is not TokenLitStr,
        f"TokenLitStr expected at {i}, given {i} {tokens}",
    )
    return ExprLitStr(tokens[i].s), i + 1


def try_parse_idf(tokens, i):
    fail_if(type(tokens[i]) is not TokenIdf,
        f"TokenIdf expected at {i}, given {i} {tokens}",
    )
    return ExprIdf(tokens[i].s), i + 1


def try_parse_braced(tokens, i):
    fail_if(type(tokens[i]) is not TokenParenOpen,
        f"TokenParenOpen expected at {i}, given {i} {tokens}",
    )
    expr, j = parse_expr(tokens, i + 1)
    fail_if(type(tokens[j]) is not TokenParenClose,
        f"TokenParenClose expected at {j}, given {i} {tokens}",
    )
    return expr, j + 1


def try_parse_lambda_1(tokens, i):
    e_idf_x, j = try_parse_idf(tokens, i)
    fail_if(type(tokens[j]) is not TokenEqGr,
        f"TokenEqGr expected at {j}, given {i} {tokens}",
    )
    expr_res, k = parse_expr(tokens, j + 1)
    return ExprLambda1(e_idf_x, expr_res), k


@tailrec
def parse_call(tokens, expr_f, i):
    fail_if(type(tokens[i]) is not TokenParenOpen,
        f"TokenParenOpen: expected, given {i} {tokens}",
    )
    fail_if(type(tokens[i + 1]) is TokenParenClose,
        f"Remove deprecated empty parenthesis, given {i} {tokens}",
    )

    expr_x, j = parse_expr(tokens, i + 1)
    fail_if(type(tokens[j]) is not TokenParenClose,
        f"TokenParenOpen expected at {j} given {i} {tokens}",
    )

    k = j + 1
    expr_call_1 = ExprCall1(expr_f, expr_x)
    return (rec(tokens, expr_call_1, k)
            if type(tokens[k]) is TokenParenOpen else
            (expr_call_1, k)
            )


@tailrec
def parse_first_of(tokens, i, parsers):
    fail_if(parsers == [], f"Can't parse Expr given {i} {tokens}.")
    either_result = rt_try(lambda: parsers[0](tokens, i))
    return (
        rec(tokens, i, parsers[1:])
        if is_fail(either_result) else
        either_result
    )


def parse_expr(tokens, i):
    expr, j = parse_first_of(tokens, i, [
        try_parse_lambda_1,
        try_parse_idf,
        try_parse_braced,
        try_parse_lit_str,
    ])
    return (parse_call(tokens, expr, j)
            if type(tokens[j]) is TokenParenOpen else
            (expr, j)
            )


def parse(tokens):
    end_of_tokens = "\0"
    ext_tokens = tokens + [end_of_tokens]
    expr, i = parse_expr(ext_tokens, 0)

    fail_if(ext_tokens[i] != end_of_tokens,
        f"Unexpected token at {i} given {tokens}",
    )

    return expr
