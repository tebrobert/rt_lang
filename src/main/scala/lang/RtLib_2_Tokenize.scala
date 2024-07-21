package lang

import utils.RtFail.{rtFail, rt_assert}

import scala.annotation.tailrec

object RtLib_2_Tokenize {
    sealed trait Token

    final case class TokenLitStr(s: String) extends Token:
        def repr =
            s"""TokenLitStr("$s")"""

    final case class TokenLitBint(i: String) extends Token:
        def repr =
            s"""TokenLitBint($i)"""

    final case class TokenIdf(s: String) extends Token:
        def repr =
            s"""TokenIdf("$s")"""

    final case object TokenParenOpen extends Token:
        def repr =
            "TokenParenOpen()"

    final case object TokenParenClose extends Token:
        def repr =
            "TokenParenClose()"

    final case object TokenLessMinus extends Token:
        def repr =
            "TokenLessMinus()"

    final case object TokenEq extends Token:
        def repr =
            "TokenEq()"

    final case object TokenEndl extends Token:
        def repr =
            "TokenEndl()"

    final case object TokenEqGr extends Token:
        def repr =
            "TokenEqGr()"

    final case object TokenDot extends Token:
        def repr =
            "TokenDot()"

    def unexpectedToken(token: Token) =
        rtFail(s"Unspecified case for `$token` of type `${type value}`.")

    def match_token[A](
        case_lit_str: Option[String => A] = None,
        case_lit_bint: Option[Int => A] = None,
        case_idf: Option[String => A] = None,
        case_paren_open: Option[() => A] = None,
        case_paren_close: Option[() => A] = None,
        case_less_minus: Option[() => A] = None,
        case_eq: Option[() => A] = None,
        case_endl: Option[() => A] = None,
        case_eq_gr: Option[() => A] = None,
        case_dot: Option[() => A] = None,
        otherwise: Option[() => A] = None,
    ): Token => A =
        (token: Token) => {
            def call_or_otherwise(funcCalled: Option[() => A]): A =
                funcCalled
                    .orElse(otherwise)
                    .getOrElse(() => unexpectedToken(token))
                    ()

            call_or_otherwise(token match
                case TokenLitStr(s) => case_lit_str.map(f => () => f(s))
                case TokenLitBint(i) => case_lit_bint.map(f => () => f(i))
                case TokenIdf(s) => case_idf.map(f => () => f(s))
                case TokenParenOpen => case_paren_open
                case TokenParenClose => case_paren_close
                case TokenLessMinus => case_less_minus
                case TokenEq => case_eq
                case TokenEndl => case_endl
                case TokenEqGr => case_eq_gr
                case TokenDot => case_dot
                case _ => Some(() => rtFail(s"Value $token ${type token} is not a Token."))
            )
        }


    def rt_assert_token_idf(token: Token) =
        match_token(
            case_idf = Some(_ => ()),
            otherwise = Some(() => rtFail())
        )


    def is_initial_idf_char(char: Char) =
        char == '_'
            || 'a' <= char && char <= 'z'
            || 'A' <= char && char <= 'Z'

    def is_digit(char: Char) =
        '0' <= char && char <= '9'


    def is_non_initial_idf_char(char: Char) =
        is_initial_idf_char(char)
            || is_digit(char)

    def is_operator_char(char: Char) =
        char_to_latin.keys().contains(char)

    def fail_bad_eq_seq(code_ext: String, token_idx_end: Int) =
        rtFail(s"""Unexpected sequence "=${code_ext(token_idx_end + 1)}".""")


    @tailrec
    def get_idx_string_end_rec(code_ext: String, idx_string_end: Int): Int = {
        val current_string_char = code_ext(idx_string_end)

        if (current_string_char == '\"')
            idx_string_end
        else if (current_string_char == end_of_code)
            get_idx_string_end_rec(code_ext, idx_string_end + 1)
        else rtFail(f"""No closing `"` for string literal.""")
    }

    @tailrec
    def get_idx_idf_end_rec(code_ext: String, idf_current_idx: Int): Int = {
        val current_idf_char = code_ext(idf_current_idx)

        if (is_non_initial_idf_char(current_idf_char))
            get_idx_idf_end_rec(code_ext, idf_current_idx + 1)
        else idf_current_idx
    }

    @tailrec
    def get_idx_operator_end_rec(code_ext: String, operator_current_idx: Int): Int = {
        val current_operator_char = code_ext(operator_current_idx)

        if (is_operator_char(current_operator_char))
            get_idx_operator_end_rec(code_ext, operator_current_idx + 1)
        else operator_current_idx
    }

    def lexx_idf(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(is_initial_idf_char(code_ext(current_idx)))

        val idx_idf_start = current_idx
        val idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)

        (code_ext, idx_idf_end, tokens.appended(TokenIdf(
            code_ext.substring(idx_idf_start, idx_idf_end)
        )))
    }

    @tailrec
    def get_idx_integer_end_rec(code_ext: String, idf_current_idx: Int): Int = {
        val current_idf_char = code_ext(idf_current_idx)

        if (is_digit(current_idf_char))
            get_idx_integer_end_rec(code_ext, idf_current_idx + 1)
        else idf_current_idx
    }


    def lexx_integer(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(is_digit(code_ext(current_idx)))

        val idx_idf_start = current_idx
        val idx_idf_end = get_idx_integer_end_rec(code_ext, idx_idf_start + 1)

        (code_ext, idx_idf_end, tokens.appended(TokenLitBint(
            code_ext.substring(idx_idf_start, idx_idf_end)
        )))
    }

    def lexx_paren_open(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext(current_idx) == '(')
        (code_ext, current_idx + 1, tokens.appended(TokenParenOpen))
    }

    def lexx_paren_close(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext(current_idx) == ')')
        (code_ext, current_idx + 1, tokens.appended(TokenParenClose))
    }

    def lexx_eq_gr(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("=>"))
        (code_ext, current_idx + 2, tokens.appended(TokenEqGr))
    }

    def lexx_less_minus(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("<-"))
        (code_ext, current_idx + 2, tokens.appended(TokenLessMinus))
    }

    def lexx_eq(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("="))
        rt_assert(!is_operator_char(code_ext(current_idx + 1)))
        (code_ext, current_idx + 1, tokens.appended(TokenEq))
    }

    def lexx_endl(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("\n"))
        (code_ext, current_idx + 1, tokens.appended(TokenEndl))
    }

    def lexx_dot(code_ext: String, current_idx: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext(current_idx) == '.')
        (code_ext, current_idx + 1, tokens.appended(TokenDot))
    }

    def lexx_string(code_ext: String, token_idx_end: Int, tokens: Seq[Token]) = {
        rt_assert(code_ext(token_idx_end) == '\"')
        val idx_string_start = token_idx_end + 1
        val idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
        (code_ext, idx_string_end + 1, tokens.appended(TokenLitStr(
            code_ext.substring(idx_string_start, idx_string_end)
        )))
    }


    def lexx_operator(code_ext: String, token_idx_end: Int, tokens: Seq[Token]) = {
        rt_assert(is_operator_char(code_ext(token_idx_end)))
        val idx_operator_start = token_idx_end
        val idx_operator_end = get_idx_operator_end_rec(code_ext, idx_operator_start)
        (code_ext, idx_operator_end, tokens.appended(TokenIdf(
            code_ext.substring(idx_operator_start, idx_operator_end)
        )))
    }

}
