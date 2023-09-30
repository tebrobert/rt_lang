from lang.lib_2_lexx import *


class ParseErr(ValueError):
    def __init__(self):
        pass


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


def try_parse_lit_str(tokens, i):
    fail_if(type(tokens[i]) is not TokenLitStr, ParseErr())
    return ExprLitStr(tokens[i].s), i + 1


def try_parse_idf(tokens, i):
    fail_if(type(tokens[i]) is not TokenIdf, ParseErr())
    return ExprIdf(tokens[i].s), i + 1


def try_parse_braced(tokens, i):
    fail_if(type(tokens[i]) is not TokenParenOpen, ParseErr())
    expr, j = parse_expr(tokens, i + 1)
    fail_if(type(tokens[j]) is not TokenParenClose,
        f"Token_Paren_Close expected at {j} given {i} {tokens}",
    )
    return expr, j + 1


def try_parse_lambda_1(tokens, i):
    e_idf_x, j = try_parse_idf(tokens, i)
    fail_if(type(tokens[j]) is not TokenEqGr, ParseErr())
    expr_res, k = parse_expr(tokens, j + 1)
    return ExprLambda1(e_idf_x, expr_res), k


@tailrec
def parse_call(tokens, expr_f, i):
    fail_if(type(tokens[i]) is not TokenParenOpen,
        f"Token_Paren_Open: expected, given {i} {tokens}",
    )
    fail_if(type(tokens[i + 1]) is TokenParenClose,
        f"Remove deprecated empty parenthesis, given {i} {tokens}",
    )

    expr_x, j = parse_expr(tokens, i + 1)
    fail_if(type(tokens[j]) is not TokenParenClose,
        f"Token_Paren_Open expected at {j} given {i} {tokens}",
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
    try:
        return parsers[0](tokens, i)
    except ParseErr:
        return rec(tokens, i, parsers[1:])


def parse_expr(tokens, i):
    expr, j = parse_first_of(tokens, i, [
        try_parse_lambda_1,  # Higher priority
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
