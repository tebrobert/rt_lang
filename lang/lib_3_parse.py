from lang.lib_0_0_lits import *
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


def is_expr(val):
    return type(val) in [
        ExprLitStr,
        ExprIdf,
        ExprCall1,
        ExprLambda1,
        ExprBraced,
    ]


def match_expr(
    case_lit_str,
    case_idf,
    case_call_1,
    case_lambda_1,
    case_braced,
):
    return lambda expr: ({
        ExprLitStr: lambda: case_lit_str(expr.s),
        ExprIdf: lambda: case_idf(expr.s),
        ExprCall1: lambda: case_call_1(expr.expr_f, expr.expr_x),
        ExprLambda1: lambda: case_lambda_1(expr.expr_idf_arg, expr.expr_res),
        ExprBraced: lambda: case_braced(expr.expr)
    }
    .get(
        type(expr),
        lambda: fail(f"Value {expr} {type(expr)} is not an Expr.")
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
        case_at_least_1=lambda head, tail: try_next_parser(head, tail),
    )(parsers)


def parse_lit_str(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenLitStr,
        f"TokenLitStr expected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return ExprLitStr(ext_tokens[current_idx].s), current_idx + 1


def new_parse_lit_str(tokens):
    return match_list(
        case_empty=lambda: fail(
            "Can't parse string literal from empty tokens.",
        ),
        case_at_least_1=lambda head, tail: (
            match_token(
                case_lit_str=lambda s: (ExprLitStr(s), tail),
                otherwise=lambda: fail(
                    f"String literal expected, got `{head}`."
                ),
            )(head)
        ),
    )(tokens)


def parse_idf(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenIdf,
        f"TokenIdf expected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return ExprIdf(ext_tokens[current_idx].s), current_idx + 1


def new_parse_idf(tokens):
    return match_list(
        case_empty=lambda: fail(
            "Can't parse string literal from empty tokens.",
        ),
        case_at_least_1=lambda head, tail: (
            match_token(
                case_idf=lambda s: (ExprIdf(s), tail),
                otherwise=lambda: fail(
                    f"Identifier expected, got `{head}`."
                ),
            )(head)
        ),
    )(tokens)


def parse_braced_full_expr(ext_tokens, current_idx):
    fail_if(type(ext_tokens[current_idx]) is not TokenParenOpen,
        f"TokenParenOpen exptokensected at {current_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    expr, paren_close_idx = old_parse_full_expr(ext_tokens, current_idx + 1)
    fail_if(type(ext_tokens[paren_close_idx]) is not TokenParenClose,
        f"TokenParenClose expected at {paren_close_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    return expr, paren_close_idx + 1


def new_parse_paren_open(tokens):
    head, tail = rt_assert_at_least_1(tokens)
    rt_assert_type(head, TokenParenOpen)
    return tail


def new_parse_paren_close(tokens):
    head, tail = rt_assert_at_least_1(tokens)
    rt_assert_type(head, TokenParenClose)
    return tail


def new_parse_braced_full_expr(tokens):
    tokens_without_parsed_paren_open = new_parse_paren_open(tokens)
    expr, tokens_without_parsed_full_expr = new_parse_full_expr(
        tokens_without_parsed_paren_open
    )
    tokens_without_parsed_paren_close = new_parse_paren_open(
        tokens_without_parsed_full_expr
    )
    rt_assert_equal(tokens_without_parsed_paren_close, [])
    return ExprBraced(expr)


def parse_lambda_1(ext_tokens, current_idx):
    e_idf_x, eq_gr_idx = parse_idf(ext_tokens, current_idx)
    fail_if(type(ext_tokens[eq_gr_idx]) is not TokenEqGr,
        f"TokenEqGr expected at {eq_gr_idx}.",
        f"Given {current_idx} {ext_tokens}",
    )
    expr_res, next_idx = old_parse_full_expr(ext_tokens, eq_gr_idx + 1)
    return ExprLambda1(e_idf_x, expr_res), next_idx


def new_parse_lambda_1(tokens):
    head0, head1, tail = rt_assert_at_least_2(tokens)
    x_idf_s = rt_assert_token_idf(head0)
    rt_assert_type(head1, TokenEqGr)
    res_expr, rest = new_parse_full_expr(tail)
    rt_assert_equal(rest, [])
    return ExprLambda1(ExprIdf(x_idf_s), res_expr)


def parse_atomic_expr(ext_tokens, current_idx):
    return get_first_success([
        parse_lambda_1,
        parse_idf,
        parse_braced_full_expr,
        parse_lit_str,
    ], ext_tokens, current_idx)


def new_parse_atomic_expr(tokens):
    return get_first_success([
        new_parse_lambda_1,
        new_parse_idf,
        new_parse_braced_full_expr,
        new_parse_lit_str,
    ], tokens)


@tailrec
def old_continue_parsing_call_rec(ext_tokens, expr_f, current_idx):
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


def old_parse_call_expr(ext_tokens, current_idx):
    parsed_atomic_expr, post_atomic_idx = parse_atomic_expr(
        ext_tokens, current_idx
    )
    return old_continue_parsing_call_rec(
        ext_tokens, parsed_atomic_expr, post_atomic_idx
    )


@tailrec
def old_continue_parsing_dotting_rec(ext_tokens, expr_acceptor, current_idx):
    def continue_parsing_dotting_forced():
        parsed_expr_method, post_braced_idx = old_parse_call_expr(
            ext_tokens, current_idx + 1
        )
        parsed_expr = ExprCall1(parsed_expr_method, expr_acceptor)
        return rec(ext_tokens, parsed_expr, post_braced_idx)

    return (
        continue_parsing_dotting_forced()
        if type(ext_tokens[current_idx]) is TokenDot else
        (expr_acceptor, current_idx)
    )


def old_parse_full_expr(ext_tokens, current_idx):
    parsed_call_expr, post_call_idx = old_parse_call_expr(ext_tokens,
        current_idx)
    return old_continue_parsing_dotting_rec(
        ext_tokens, parsed_call_expr, post_call_idx
    )


@tailrec
def new_preparse_idf_lit(tokens_and_exprs, acc=[]):
    return match_list(
        case_at_least_1=lambda head, tail: rec(tail, acc + [
            head if is_expr(head) else match_token(
                case_lit_str=lambda s: ExprLitStr(s),
                case_idf=lambda s: ExprIdf(s),
                otherwise=lambda: head,
            )(head)
        ]),
        case_empty=lambda: acc,
    )(tokens_and_exprs)


@tailrec
def new_continue_preparse_braced(
    ext_tokens_and_exprs, acc, acc_braced,
    unclosed_parens_count,
):
    return match_list(
        case_empty=lambda: fail("`)` expected."),
        case_at_least_1=lambda head, tail: (
            (
                rec(tail, acc, acc_braced + [head], unclosed_parens_count - 1)
                if unclosed_parens_count > 1 else
                (tail, acc + [ExprBraced(new_parse_full_expr(acc_braced))])
            )
            if head == TokenParenClose() else
            rec(tail, acc, acc_braced + [head], unclosed_parens_count + 1)
            if head == TokenParenOpen() else
            rec(tail, acc, acc_braced + [head], unclosed_parens_count)
        )
    )(ext_tokens_and_exprs)


@tailrec
def new_preparse_braced(tokens_and_exprs, acc=[]):
    return match_list(
        case_empty=lambda: acc,
        case_at_least_1=lambda head, tail: (
            rec(*new_continue_preparse_braced(tail, acc, [], 1))
            if head == TokenParenOpen() else
            rec(tail, acc + [head])
        )
    )(tokens_and_exprs)


@tailrec
def new_preparse_call(tokens_and_exprs, acc_rest=[]):
    return match_list(
        case_at_least_3=lambda head0, head1, head2, tail2: (
            rec([head1, head2] + tokens_and_exprs, acc_rest + [head0])
            if type(head2) is not ExprBraced else
            (
                rec([head2] + tail2, acc_rest + [head0, head1])
                if is_expr(head0) else
                rec([head0, ExprCall1(head1, head2)] + tail2, acc_rest)
            )
            if type(head1) in ExprIdf else
            rec([head0, ExprCall1(head1, head2)] + tail2, acc_rest)
            if is_expr(type(head1)) else
            rec([head1, head2] + tokens_and_exprs, acc_rest + [head0])
        ),
        case_at_least_2=lambda head0, head1, tail1: (
            rec([ExprCall1(head0, head1)] + tail1, acc_rest)
            if is_expr(head0) and type(head1) is ExprBraced else
            rec([head1] + tail1, acc_rest + [head0])
        ),
        case_at_least_1=lambda head0, _tail: acc_rest + [head0],
        case_empty=lambda: acc_rest,
    )(tokens_and_exprs)


@tailrec
def new_preparse(tokens_and_exprs, preparsers):
    return match_list(
        case_empty=lambda: tokens_and_exprs,
        case_at_least_1=lambda head, tail: rec(head(tokens_and_exprs), tail)
    )(preparsers)


def new_parse_full_expr(tokens):
    preparsed = new_preparse(tokens, [
        new_preparse_idf_lit,
        new_preparse_braced,
        new_preparse_call,
        # preparse_dot,
        # preparse_plus_minus,
        # old_parse_single_expr,
    ])
    head_preparsed, tail_preparsed = rt_assert_at_least_1(preparsed)
    rt_assert_empty(tail_preparsed)
    rt_assert(is_expr(head_preparsed))
    return head_preparsed





def parse_line_with_less_minus(current_line, next_lines_expr):
    idf = rt_assert_type(current_line[0], TokenIdf)
    rt_assert_type(current_line[1], TokenLessMinus)
    right_expr = old_parse_single_expr(current_line[2:])
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
    right_expr = old_parse_single_expr(current_line[2:])
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
    right_expr = old_parse_single_expr(current_line)
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
        case_at_least_1=lambda head, tail: rec(
            tail, get_first_success([
                parse_line_with_less_minus,
                parse_line_with_equals,
                parse_effectful_line,
            ], head, acc_expr)
        ),
    )(lines_reversed)


def old_parse_single_expr(tokens):
    ext_tokens = tokens + [end_of_tokens]
    expr, current_idx = old_parse_full_expr(ext_tokens, 0)

    fail_if(ext_tokens[current_idx] != end_of_tokens,
        f"Unexpected token at {current_idx} given {tokens}",
    )

    return expr


def new_parse_single_expr(tokens):
    preparsed = new_parse_full_expr(tokens)
    first_expr, rest_exprs = rt_assert_at_least_1(preparsed)
    return first_expr, rest_exprs


@tailrec
def get_lines_reversed(tokens_reversed, acc_lines=[], acc_current_line=[]):
    return match_list(
        case_empty=lambda: acc_lines + [acc_current_line],
        case_at_least_1=lambda head, tail: (
            rec(tail, acc_lines + [acc_current_line], [])
            if head == TokenEndl() else
            rec(tail, acc_lines, [head] + acc_current_line)
        ),
    )(tokens_reversed)


def parse(tokens):
    tokens_reversed = list(reversed(tokens))
    lines_reversed = get_lines_reversed(tokens_reversed)
    nonempty_lines_reversed = list(filter(len, lines_reversed))
    return match_list(
        case_empty=lambda: fail("Yet empty file is unsupported."),
        case_at_least_1=lambda head, tail: parse_previous_lines(
            tail, new_parse_full_expr(head)
        ),
    )(nonempty_lines_reversed)


def full_parse(code):
    return parse(full_tokenize(code))


end_of_tokens = "\0"
