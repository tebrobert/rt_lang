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
        rt_assert(is_expr(expr_x))
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


@tailrec
def apply_all(funcs, args):
    return match_list(
        case_empty=lambda: args,
        case_at_least_1=lambda head, tail: rec(tail, head(args))
    )(funcs)


def parse_lit_str(tokens):
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


def parse_idf(tokens):
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


def parse_paren_open(tokens):
    head, tail = rt_assert_at_least_1(tokens)
    rt_assert_type(head, TokenParenOpen)
    return tail


def parse_paren_close(tokens):
    head, tail = rt_assert_at_least_1(tokens)
    rt_assert_type(head, TokenParenClose)
    return tail


def parse_braced_full_expr(tokens):
    tokens_without_parsed_paren_open = parse_paren_open(tokens)
    expr, tokens_without_parsed_full_expr = parse_full_expr(
        tokens_without_parsed_paren_open
    )
    tokens_without_parsed_paren_close = parse_paren_open(
        tokens_without_parsed_full_expr
    )
    rt_assert_equal(tokens_without_parsed_paren_close, [])
    return ExprBraced(expr)


def parse_lambda_1(tokens):
    head0, head1, tail = rt_assert_at_least_2(tokens)
    x_idf_s = rt_assert_token_idf(head0)
    rt_assert_type(head1, TokenEqGr)
    res_expr, rest = parse_full_expr(tail)
    rt_assert_equal(rest, [])
    return ExprLambda1(ExprIdf(x_idf_s), res_expr)


def parse_atomic_expr(tokens):
    return get_first_success([
        parse_lambda_1,
        parse_idf,
        parse_braced_full_expr,
        parse_lit_str,
    ], tokens)


@tailrec
def preparse_idf_lit(tokens_and_exprs, acc=[]):
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
def continue_preparse_braced(
    ext_tokens_and_exprs, acc, acc_braced,
    unclosed_parens_count,
):
    return match_list(
        case_empty=lambda: fail("`)` expected."),
        case_at_least_1=lambda head, tail: (
            (
                rec(tail, acc, acc_braced + [head], unclosed_parens_count - 1)
                if unclosed_parens_count > 1 else
                (tail, acc + [ExprBraced(parse_full_expr(acc_braced))])
            )
            if head == TokenParenClose() else
            rec(tail, acc, acc_braced + [head], unclosed_parens_count + 1)
            if head == TokenParenOpen() else
            rec(tail, acc, acc_braced + [head], unclosed_parens_count)
        )
    )(ext_tokens_and_exprs)


@tailrec
def preparse_braced(tokens_and_exprs, acc=[]):
    return match_list(
        case_empty=lambda: acc,
        case_at_least_1=lambda head, tail: (
            rec(*continue_preparse_braced(tail, acc, [], 1))
            if head == TokenParenOpen() else
            rec(tail, acc + [head])
        )
    )(tokens_and_exprs)


def debrace_expr(expr):
    return match_expr(
        case_lit_str=lambda _s: expr,
        case_idf=lambda _s: expr,
        case_call_1=lambda f, x: ExprCall1(
            debrace_expr(f),
            debrace_expr(x),
        ),
        case_lambda_1=lambda arg, res: ExprLambda1(
            debrace_expr(arg),
            debrace_expr(res),
        ),
        case_braced=lambda inner_expr: debrace_expr(inner_expr),
    )(expr)


@tailrec
def preparse_debrace(tokens_and_exprs, acc=[]):
    return match_list(
        case_at_least_1=lambda head, tail: (
            rec(tail, acc + [debrace_expr(head)])
            if is_expr(head) else
            rec(tail, acc + [head])
        ),
        case_empty=lambda: acc,
    )(tokens_and_exprs)

@tailrec
def preparse_call(tokens_and_exprs, acc=[]):
    return match_list(
        case_at_least_3=lambda head0, head1, head2, tail2: (
            rec([ExprCall1(head0, head1), head2] + tail2, acc)
            if is_expr(head0) and type(head1) is ExprBraced else
            rec([head1, head2] + tail2, acc + [head0])
            if type(head1) is not ExprIdf else
            rec([head2] + tail2, acc + [head0, head1])
            if is_expr(head0) else
            rec([head1, head2] + tail2, acc + [head0])
        ),
        case_at_least_2=lambda head0, head1, tail1: (
            rec([ExprCall1(head0, head1)] + tail1, acc)
            if is_expr(head0) and type(head1) is ExprBraced else
            rec([head1] + tail1, acc + [head0])
        ),
        case_at_least_1=lambda head0, _tail: acc + [head0],
        case_empty=lambda: acc,
    )(tokens_and_exprs)


@tailrec
def preparse_dot(tokens_and_exprs, acc=[]):
    return match_list(
        case_at_least_3=lambda head0, head1, head2, tail2: (
            rec([ExprCall1(head2, head0)] + tail2, acc)
            if is_expr(head0) and type(head1) is TokenDot and is_expr(
                head2
            ) else
            rec([head1, head2] + tail2, acc + [head0])
        ),
        case_at_least_2=lambda head0, head1, _tail1: acc + [head0, head1],
        case_at_least_1=lambda head0, _tail0: acc + [head0],
        case_empty=lambda: acc,
    )(tokens_and_exprs)


@tailrec
def preparse_lambda_reversed_rec(reversed_tokens_and_exprs, acc=[]):
    return match_list(
        case_at_least_3=lambda head0, head1, head2, tail2: (
            rec([ExprLambda1(head2, head0)] + tail2, acc)
            if is_expr(head0) and type(head1) is TokenEqGr and is_expr(
                head2
            ) else
            rec([head1, head2] + tail2, acc + [head0])
        ),
        case_at_least_2=lambda head0, head1, _tail1: acc + [head0, head1],
        case_at_least_1=lambda head0, _tail0: acc + [head0],
        case_empty=lambda: acc,
    )(reversed_tokens_and_exprs)


def preparse_lambda(tokens_and_exprs):
    return list(reversed(preparse_lambda_reversed_rec(list(reversed(
        tokens_and_exprs
    )))))

def parse_full_expr(tokens):
    preparsed = apply_all([
        preparse_idf_lit,
        preparse_braced,
        preparse_call,
        preparse_debrace,
        preparse_dot,
        # preparse_plus_minus,
        preparse_lambda,
    ], tokens)
    head_preparsed, tail_preparsed = rt_assert_at_least_1(preparsed)
    rt_assert_empty(tail_preparsed)
    rt_assert(is_expr(head_preparsed))
    return head_preparsed


def parse_line_with_less_minus(current_line, next_lines_expr):
    idf = rt_assert_type(current_line[0], TokenIdf)
    rt_assert_type(current_line[1], TokenLessMinus)
    right_expr = parse_full_expr(current_line[2:])
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
    right_expr = parse_full_expr(current_line[2:])
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
    right_expr = parse_full_expr(current_line)
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
            tail, parse_full_expr(head)
        ),
    )(nonempty_lines_reversed)


def full_parse(code):
    return parse(full_tokenize(code))


end_of_tokens = "\0"
