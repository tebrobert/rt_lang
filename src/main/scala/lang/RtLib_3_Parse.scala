package lang

import lang.RtLib_0_0_Lits.*
import lang.RtLib_2_Tokenize.{Token, TokenDot, TokenEndl, TokenEq, TokenEqGr, TokenIdf, TokenLessMinus, TokenLitBint, TokenLitStr, TokenParenClose, TokenParenOpen}
import utils.RtFail.{rtFail, rt_assert, rt_assert_type, try_and_match}
import utils.RtList.{match_list, rt_assert_at_least_1, rt_assert_empty}

object RtLib_3_Parse {
    sealed trait Expr {
        def repr(indent: String = ""): String
    }

    final case class ExprLitStr(s: String) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            s"""${indent}Expr_Lit_Str("$s")"""

    final case class ExprLitBint(i: String) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            s"""${indent}ExprLitBint($i)"""

    final case class ExprIdf(s: String) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            s"""${indent}Expr_Idf("$s")"""

    final case class ExprCall1(expr_f: Expr, expr_x: Expr) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            (     s"""${indent}Expr_Call_1($typified_repr_endl"""
                + s"""${expr_f.repr(indent + " "*4)},$typified_repr_endl"""
                + s"""${expr_x.repr(indent + " "*4)}$typified_repr_endl"""
                + s"""$indent)"""
                )

    final case class ExprLambda1(expr_idf_arg: ExprIdf, expr_res: Expr) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            (     s"""${indent}Expr_Lambda_1($typified_repr_endl"""
                + s"""${expr_idf_arg.repr(indent + " "*4)},$typified_repr_endl"""
                + s"""${expr_res.repr(indent + " "*4)}$typified_repr_endl"""
                + s"""$indent)"""
                )

    final case class ExprBraced(expr: Expr) extends Expr:
        def repr(indent: String = "") =
            //if expr_repr_flat(): pass
            (     s"""${indent}ExprBraced($typified_repr_endl"""
                + s"""${expr.repr(indent + " "*4)}$typified_repr_endl"""
                + s"""$indent)"""
                )

    def is_expr(expr: Expr): Boolean =
        true

    //def expr_repr_flat() =
    //    false //or true

    def match_expr[A](
        case_lit_str: String => A,
        case_lit_bint: String => A,
        case_idf: String => A,
        case_call_1: (Expr, Expr) => A,
        case_lambda_1: (ExprIdf, Expr) => A,
        case_braced: Expr => A,
    ): Expr => A =
        (expr: Expr) => expr match
            case ExprLitStr(s) => case_lit_str(s)
            case ExprLitBint(i) => case_lit_bint(i)
            case ExprIdf(s) => case_idf(s)
            case ExprCall1(expr_f, expr_x) => case_call_1(expr_f, expr_x)
            case ExprLambda1(expr_idf_arg, expr_res) => case_lambda_1(expr_idf_arg, expr_res)
            case ExprBraced(expr) => case_braced(expr)

    //@tailrec
    def get_first_success(
        parsers: List[(List[Token], Expr) => Expr],
        parser_args: (List[Token], Expr),
        fails: List[String] = List(),
    ): Expr = {
        def try_next_parser(
            current_parser: (List[Token], Expr) => Expr,
            rest_parsers: List[(List[Token], Expr) => Expr],
        ): Expr =
            try_and_match(
                () => current_parser.tupled(parser_args),
                identity,
                () => get_first_success(rest_parsers, parser_args, fails = fails /*+[either_result]*/)
            )

        match_list(
            case_empty = Some(() => rtFail(
                f"Can't parse Expr given `{parser_args}`.",
                //s"Fails - `{fails}`."
            )),
            case_at_least_1 = Some((head, tail) => try_next_parser(head, tail)),
        )(parsers)
    }

    //@tailrec
    def apply_all(
      funcs: List[(List[Token | Expr], List[Token | Expr]) => List[Token | Expr]],
      args: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[
          (List[Token | Expr], List[Token | Expr]) => List[Token | Expr],
          List[Token | Expr]
        ](
            case_empty = Some(() => args),
            case_at_least_1 = Some((head, tail) => apply_all(tail, head(args, List())))
        ) (funcs)

    //@tailrec
    def preparse_idf_lit(
        tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr]
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_at_least_1=Some((head, tail) => preparse_idf_lit(tail, acc :+ (
                head match {
                    case expr: Expr => expr
                    case TokenLitStr(s) => ExprLitStr(s)
                    case TokenLitBint(i) => ExprLitBint(i)
                    case TokenIdf(s) => ExprIdf(s)
                    case _ => head
                }
            ))),
            case_empty=Some(() => acc),
        )(tokens_and_exprs)

    //@tailrec
    def continue_preparse_braced(
        ext_tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
        acc_braced: List[Token | Expr],
        unclosed_parens_count: Int,
    ): (List[Token | Expr], List[Token | Expr]) =
        match_list[Token | Expr, (List[Token | Expr], List[Token | Expr])](
            case_empty=rtFail("`)` expected."),
            case_at_least_1=Some((head, tail) =>
                  if (unclosed_parens_count > 1)
                    continue_preparse_braced(tail, acc, acc_braced :+ head, unclosed_parens_count - 1)
                  else if (head == TokenParenClose)
                    (tail, acc :+ ExprBraced(parse_full_expr(acc_braced)))
                  else if (head == TokenParenOpen)
                    continue_preparse_braced(tail, acc, acc_braced :+ head, unclosed_parens_count + 1)
                  else continue_preparse_braced(tail, acc, acc_braced :+ head, unclosed_parens_count)
            )
        )(ext_tokens_and_exprs)

    //@tailrec
    def preparse_braced(
        tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_empty=Some(() => acc),
            case_at_least_1=Some((head, tail) => (
                if (head == TokenParenOpen)
                    preparse_braced.tupled(continue_preparse_braced(tail, acc, List(), 1))
                else preparse_braced(tail, acc :+ head)
            ))
        )(tokens_and_exprs)

    def debrace_expr(expr: Expr): Expr =
        match_expr(
            case_lit_str = _s => expr,
            case_lit_bint= _i => expr,
            case_idf= _s => expr,
            case_call_1= (f, x) => ExprCall1(
                debrace_expr(f),
                debrace_expr(x),
            ),
            case_lambda_1=(arg, res) => ExprLambda1(
                arg, //debrace_expr(arg),
                debrace_expr(res),
            ),
            case_braced=inner_expr => debrace_expr(inner_expr),
        )(expr)

    //@tailrec
    def preparse_debrace(
        tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_at_least_1=Some((head, tail) =>
              head match {
                  case expr: Expr => preparse_debrace(tail, acc :+ debrace_expr(expr))
                  case _ => preparse_debrace(tail, acc :+ head)
               }
            ),
            case_empty=Some(() => acc),
        )(tokens_and_exprs)

    //@tailrec
    def preparse_call(
        tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_at_least_3=Some((head0, head1, head2, tail2) =>
                (head0, head1) match {
                    case (expr0: Expr, exprBraced1: ExprBraced) =>
                        preparse_call(List(ExprCall1(expr0, exprBraced1), head2) ++ tail2, acc)
                    case _ if !head1.isInstanceOf[ExprIdf] =>
                        preparse_call(List(head1, head2) ++ tail2, acc ++ List(head0))
                    case _ if head0.isInstanceOf[Expr] =>
                        preparse_call(head2 +: tail2, acc ++ List(head0, head1))
                    case _ => preparse_call(List(head1, head2) ++ tail2, acc :+ head0)
                }
            ),
            case_at_least_2=Some((head0, head1, tail1) =>
              (head0, head1) match {
                  case (expr0: Expr, exprBraced1: ExprBraced) =>
                      preparse_call(ExprCall1(expr0, exprBraced1) +: tail1, acc)
                  case _ => preparse_call(head1 +: tail1, acc :+ head0)
              }
            ),
            case_at_least_1=Some((head0, _tail) => acc :+ head0),
            case_empty=Some(() => acc),
        )(tokens_and_exprs)

    //@tailrec
    def preparse_dot(
        tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_at_least_3=Some((head0, head1, head2, tail2) =>
                (head0, head1, head2) match {
                    case (expr0: Expr, TokenDot, expr2: Expr) =>
                        preparse_dot(ExprCall1(expr2, expr0) +: tail2, acc)
                    case _ => preparse_dot(List(head1, head2) ++ tail2, acc :+ head0)
                }
            ),
            case_at_least_2=Some((head0, head1, _tail1) => acc ++ List(head0, head1)),
            case_at_least_1=Some((head0, _tail0) => acc :+ head0),
            case_empty=Some(() => acc),
        )(tokens_and_exprs)

    //@tailrec
    def preparse_lambda_reversed_rec(
        reversed_tokens_and_exprs: List[Token | Expr],
        acc: List[Token | Expr],
    ): List[Token | Expr] =
        match_list[Token | Expr, List[Token | Expr]](
            case_at_least_3=Some((head0, head1, head2, tail2) =>
                (head0, head1, head2) match {
                    case (expr0: Expr, TokenEqGr, exprIdf2: /*Expr*/ ExprIdf ) =>
                        preparse_lambda_reversed_rec(ExprLambda1(exprIdf2, expr0) +: tail2, acc)
                    case _ => preparse_lambda_reversed_rec(List(head1, head2) ++ tail2, acc :+ head0)
                }
            ),
            case_at_least_2=Some((head0, head1, _tail1) => acc ++ List(head0, head1)),
            case_at_least_1=Some((head0, _tail0) => acc :+ head0),
            case_empty=Some(() => acc),
        )(reversed_tokens_and_exprs)

    def preparse_lambda(
        tokens_and_exprs: List[Token | Expr],
        /**/ _acc: List[Token | Expr],
    ): List[Token | Expr] =
        preparse_lambda_reversed_rec(
            tokens_and_exprs.reverse, List()
        ).reverse


    def preparse_left_to_right(operator_strings: String*) = {
        //@tailrec
        def preparser(
            tokens_and_exprs: List[Token | Expr],
            acc: List[Token | Expr],
        ): List[Token | Expr] =
            match_list[Token | Expr, List[Token | Expr]](
                case_at_least_3=Some((head0, head1, head2, tail2) =>
                    (head0, head1, head2) match {
                        case (expr0: Expr, ExprIdf(s1), expr2: Expr)
                            if operator_strings.contains(s1) =>
                            preparser(ExprCall1(ExprCall1(ExprIdf(s1), expr2), expr0) +: tail2, acc)
                        case _ => preparser(List(head1, head2) ++ tail2, acc :+ head0)
                    }
                ),
                case_at_least_2=Some((head0, head1, _tail) => acc ++ List(head0, head1)),
                case_at_least_1=Some((head0, _tail) => acc :+ head0),
                case_empty=Some(() => acc),
            )(tokens_and_exprs)

        preparser
    }

    def preparse_unary(operator: String)
    : (List[Token | Expr], List[Token | Expr]) => List[Token | Expr] = {
        def preparser(
            tokens_and_exprs: List[Token | Expr],
            _acc: List[Token | Expr],
        ): List[Token | Expr] =
            match_list[Token | Expr, List[Token | Expr]](
                case_at_least_2=Some((head0, head1, tail1) =>
                    (head0, head1) match {
                        case (ExprIdf(`operator`), expr1: Expr) =>
                            ExprCall1(ExprIdf(operator), expr1) +: tail1
                        case _ => tokens_and_exprs
                    }
                ),
                otherwise=Some(() => tokens_and_exprs),
            )(tokens_and_exprs)

        preparser
    }

    def parse_full_expr(tokens: List[Token | Expr]): Expr = {
        val preparsed = apply_all(List(
            preparse_idf_lit,
            preparse_braced,
            preparse_call,
            preparse_debrace,
            preparse_dot,
            preparse_unary(builtin_minus),
            preparse_unary(builtin_not),
            preparse_left_to_right(builtin_multiply, builtin_div, builtin_floor_div, builtin_mod),
            preparse_left_to_right(builtin_plus, builtin_minus),
            preparse_left_to_right(builtin_less, builtin_less_eq, builtin_gr, builtin_gr_eq),
            preparse_left_to_right(builtin_eq_eq, builtin_not_eq),
            preparse_left_to_right(builtin_and),
            preparse_left_to_right(builtin_or),
            preparse_lambda,
        ), tokens)

        val (head_preparsed, tail_preparsed) = rt_assert_at_least_1(preparsed)
        rt_assert_empty(tail_preparsed)

        head_preparsed match {
            case expr: Expr => expr
            case _: Token => rtFail(s"got token `$head_preparsed`")
        }
    }

    def parse_line_with_less_minus(
        current_line: List[Token],
        next_lines_expr: Expr,
    ): ExprCall1 = {
        val idf = rt_assert_type[TokenIdf](current_line(0)) // todo unsafe
        rt_assert_type[TokenLessMinus.type](current_line(1)) // todo unsafe
        val right_expr = parse_full_expr(current_line.drop(2))
        ExprCall1(
            ExprCall1(
                ExprIdf(builtin_flatmap),
                ExprLambda1(ExprIdf(idf.s), next_lines_expr),
            ),
            right_expr,
        )
    }

    def parse_line_with_equals(
        current_line: List[Token],
        next_lines_expr: Expr,
    ): ExprCall1 = {
        val idf = rt_assert_type[TokenIdf](current_line(0)) // todo unsafe
        rt_assert_type[TokenEq.type](current_line(1)) // todo unsafe
        val right_expr = parse_full_expr(current_line.drop(2))
        ExprCall1(
            ExprCall1(
                ExprIdf(builtin_flatmap),
                ExprLambda1(ExprIdf(idf.s), next_lines_expr),
            ),
            ExprCall1(
                ExprIdf(builtin_pure),
                right_expr,
            ),
        )
    }

    def parse_effectful_line(
        current_line: List[Token],
        next_lines_expr: Expr,
    ): ExprCall1 = {
        val right_expr = parse_full_expr(current_line)
        ExprCall1(
            ExprCall1(
                ExprIdf(builtin_flatmap),
                ExprLambda1(ExprIdf("_"), next_lines_expr),
            ),
            right_expr,
        )
    }

    //@tailrec
    def parse_previous_lines(
        lines_reversed: List[List[Token]],
        acc_expr: Expr,
    ): Expr =
        match_list[List[Token], Expr](
            case_empty=Some(() => acc_expr),
            case_at_least_1=Some((lastLine, prevLines) => parse_previous_lines(
                prevLines,
                get_first_success(
                    List(
                        parse_line_with_less_minus,
                        parse_line_with_equals,
                        parse_effectful_line,
                    ),
                    (lastLine, acc_expr),
                    fails = List(),
                )
            )),
        )(lines_reversed)

    //@tailrec
    def get_lines_reversed(
        tokens_reversed: List[Token],
        acc_lines: List[List[Token]],
        acc_current_line: List[Token],
    ): List[List[Token]] =
        match_list[Token, List[List[Token]]](
            case_empty=Some(() => acc_lines :+ acc_current_line),
            case_at_least_1=Some((head, tail) =>
              if (head == TokenEndl)
                get_lines_reversed(tail, acc_lines :+ acc_current_line, List())
              else get_lines_reversed(tail, acc_lines, head +: acc_current_line)
            ),
        )(tokens_reversed)

    // left 2




    val typified_repr_endl = "\n"
}
