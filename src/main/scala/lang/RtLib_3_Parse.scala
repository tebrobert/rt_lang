package lang

import lang.RtLib_2_Tokenize.{Token, TokenIdf, TokenLitBint, TokenLitStr, TokenParenOpen, TokenParenClose}
import utils.RtFail.{rtFail, try_and_match}
import utils.RtList.match_list

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
    def get_first_success[A, B](
        parsers: List[List[A] => B],
        parser_args: List[A]/**/,
        fails: List[String] = List(),
    ): B = {
        def try_next_parser(
            current_parser: List[A] => B,
            rest_parsers: List[List[A] => B],
        ): B =
            try_and_match(
                () => current_parser(parser_args),
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


    // left 12

    // ...
    def parse_full_expr(tokens: List[Token | Expr]): Expr =
        ???


    val typified_repr_endl = "\n"
}
