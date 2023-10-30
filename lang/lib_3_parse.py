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


class ExprBraced:
    def __init__(self, expr):
        self.expr = expr

    def __repr__(self, indent=''):
        return (f"{indent}ExprBraced(\n"
                + f"""{self.expr.__repr__(indent + 4 * " ")}\n"""
                + f"""{indent})"""
                )

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
def get_first_success(parsers, *parser_args, fails=[]):
    def try_next_parser(current_parser, rest_parsers):
        either_result = rt_try(lambda: current_parser(*parser_args))
        return (
            rec(rest_parsers, *parser_args, fails=fails + [either_result])
            if is_fail(either_result) else
            either_result
        )

    return match_list(
        case_empty=lambda: fail(
            f"Can't parse Expr given `{parser_args}`.",
            # f"Fails - `{fails}`.",
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
    expr, paren_close_idx = parse_full_expr1(ext_tokens, current_idx + 1)
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
    expr_res, next_idx = parse_full_expr1(ext_tokens, eq_gr_idx + 1)
    return ExprLambda1(e_idf_x, expr_res), next_idx


def parse_atomic_expr(ext_tokens, current_idx):
    return get_first_success([
        parse_lambda_1,
        parse_idf,
        parse_braced_full_expr,
        parse_lit_str,
    ], ext_tokens, current_idx)


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


def parse_full_expr1(ext_tokens, current_idx):
    parsed_call_expr, post_call_idx = parse_call_expr(ext_tokens, current_idx)
    return continue_parsing_dotting_rec(
        ext_tokens, parsed_call_expr, post_call_idx
    )


@tailrec
def parse_full_expr_rec2(ext_tokens_and_exprs, parsers):
    def extract():
        rt_assert_equal(len(ext_tokens_and_exprs), 1)
        rt_assert(type(ext_tokens_and_exprs[0]) in [ExprCall1, ExprIdf])
        return ext_tokens_and_exprs[0]
    match_list(
        case_empty=lambda: extract(),
        case_nonempty=lambda head, tail: rec(head(ext_tokens_and_exprs), tail)
    )(parsers)


def parse_full_expr2(ext_tokens):
    parse_full_expr_rec2(ext_tokens, [
        preparse_braced,
        preparse_call,
        preparse_dot,
        preparse_plus_minus,
    ])


def parse_line_with_less_minus(current_line, next_lines_expr):
    idf = rt_assert_type(current_line[0], TokenIdf)
    rt_assert_type(current_line[1], TokenLessMinus)
    right_expr = parse_single_expr(current_line[2:])
    return ExprCall1(
        ExprCall1(
            ExprIdf(builtin_flatmap),
            ExprLambda1(ExprIdf(idf.s), next_lines_expr),
        ),
        right_expr,
    )


def parse_line_with_equals(current_line, next_lines_expr):
    idf = rt_assert_type(current_line[0], TokenIdf)
    rt_assert_type(current_line[1], TokenEq)
    right_expr = parse_single_expr(current_line[2:])
    return ExprCall1(
        ExprCall1(
            ExprIdf(builtin_flatmap),
            ExprLambda1(ExprIdf(idf.s), next_lines_expr),
        ),
        ExprCall1(
            ExprIdf(builtin_pure),
            right_expr,
        ),
    )


def parse_effectful_line(current_line, next_lines_expr):
    right_expr = parse_single_expr(current_line)
    return ExprCall1(
        ExprCall1(
            ExprIdf(builtin_flatmap),
            ExprLambda1(ExprIdf("_"), next_lines_expr),
        ),
        right_expr,
    )


@tailrec
def parse_previous_lines(lines_reversed, acc_expr):
    return match_list(
        case_empty=lambda: acc_expr,
        case_nonempty=lambda head, tail: rec(
            tail, get_first_success([
                parse_line_with_less_minus,
                parse_line_with_equals,
                parse_effectful_line,
            ], head, acc_expr)
        ),
    )(lines_reversed)


def parse_single_expr(tokens):
    ext_tokens = tokens + [end_of_tokens]
    expr, current_idx = parse_full_expr1(ext_tokens, 0)

    fail_if(ext_tokens[current_idx] != end_of_tokens,
        f"Unexpected token at {current_idx} given {tokens}",
    )

    return expr


@tailrec
def get_lines_reversed(ext_tokens_reversed, acc_lines=[], acc_current_line=[]):
    return match_list(
        case_empty=lambda: acc_lines + [acc_current_line],
        case_nonempty=lambda head, tail: (
            rec(tail, acc_lines + [acc_current_line], [])
            if head == TokenEndl() else
            rec(tail, acc_lines, [head] + acc_current_line)
        ),
    )(ext_tokens_reversed)


def parse(tokens):
    ext_tokens_reversed = list(reversed([TokenEndl()] + tokens))
    lines_reversed = get_lines_reversed(ext_tokens_reversed)
    nonempty_lines_reversed = list(filter(len, lines_reversed))
    return match_list(
        case_empty=lambda: fail("Yet empty file is unsupported."),
        case_nonempty=lambda head, tail: parse_previous_lines(
            tail, parse_single_expr(head)
        ),
    )(nonempty_lines_reversed)


def full_parse(code):
    return parse(full_tokenize2(code))


end_of_tokens = "\0"
