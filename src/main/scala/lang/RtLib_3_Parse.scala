package lang

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


    val typified_repr_endl = "\n"
}
