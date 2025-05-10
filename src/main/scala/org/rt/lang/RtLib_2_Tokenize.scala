package org.rt.lang

import org.rt.utils.RtFail.{rtFail, rt_assert, try_and_match}
import org.rt.utils.RtList.match_list

import scala.annotation.tailrec

object RtLib_2_Tokenize {
    sealed trait Token

    final case class TokenLitStr(s: String) extends Token

    final case class TokenLitBint(i: String) extends Token

    final case class TokenIdf(s: String) extends Token

    final case object TokenParenOpen extends Token

    final case object TokenParenClose extends Token

    final case object TokenLessMinus extends Token

    final case object TokenEq extends Token

    final case object TokenEndl extends Token

    final case object TokenEqGr extends Token

    final case object TokenDot extends Token


    private val end_of_code: Char = 0

    private val char_to_latin = Map(
        "+" -> "plus_",
        "-" -> "minus_",
        "*" -> "star_",
        "/" -> "slash_",
        "%" -> "percent_",
        ">" -> "greater_",
        "<" -> "less_",
        "=" -> "equal_",
        "!" -> "exclamation_",
        "~" -> "tilda_",
        "|" -> "or_",
        "&" -> "and_",
    )

    private val all_tokenizers: List[Tokenizer] = List(
        lexx_idf,
        lexx_integer,
        lexx_paren_open,
        lexx_paren_close,
        lexx_eq_gr,
        lexx_eq,
        lexx_less_minus,
        lexx_endl,
        lexx_string,
        lexx_operator,
        lexx_dot,
    )


    private def unexpectedToken(token: Token) =
        rtFail(s"Unspecified case for `$token` of type `${type value}`.")

    private def match_token[A](
        case_lit_str: Option[String => A] = None,
        case_lit_bint: Option[String => A] = None,
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


    private def rt_assert_token_idf(token: Token) =
        match_token(
            case_idf = Some(_ => ()),
            otherwise = Some(() => rtFail())
        )


    private def is_initial_idf_char(char: Char) =
        char == '_'
            || 'a' <= char && char <= 'z'
            || 'A' <= char && char <= 'Z'

    private def is_digit(char: Char) =
        '0' <= char && char <= '9'


    private def is_non_initial_idf_char(char: Char) =
        is_initial_idf_char(char)
            || is_digit(char)

    private def is_operator_char(char: Char) =
        char_to_latin.keys.toSeq.contains(char.toString)

    private def fail_bad_eq_seq(code_ext: String, token_idx_end: Int) =
        rtFail(s"""Unexpected sequence "=${code_ext(token_idx_end + 1)}".""")


    @tailrec
    private def get_idx_string_end_rec(code_ext: String, idx_string_end: Int): Int = {
        val current_string_char = code_ext(idx_string_end)

        if (current_string_char == '\"')
            idx_string_end
        else if (current_string_char == end_of_code)
            rtFail(f"""No closing `"` for string literal.""")
        else get_idx_string_end_rec(code_ext, idx_string_end + 1)
    }

    @tailrec
    private def get_idx_idf_end_rec(code_ext: String, idf_current_idx: Int): Int = {
        val current_idf_char = code_ext(idf_current_idx)

        if (is_non_initial_idf_char(current_idf_char))
            get_idx_idf_end_rec(code_ext, idf_current_idx + 1)
        else idf_current_idx
    }

    @tailrec
    private def get_idx_operator_end_rec(code_ext: String, operator_current_idx: Int): Int = {
        val current_operator_char = code_ext(operator_current_idx)

        if (is_operator_char(current_operator_char))
            get_idx_operator_end_rec(code_ext, operator_current_idx + 1)
        else operator_current_idx
    }

    private def lexx_idf(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(is_initial_idf_char(code_ext(current_idx)))

        val idx_idf_start = current_idx
        val idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)

        (code_ext, idx_idf_end, tokens.appended(TokenIdf(
            code_ext.substring(idx_idf_start, idx_idf_end)
        )))
    }

    @tailrec
    private def get_idx_integer_end_rec(code_ext: String, idf_current_idx: Int): Int = {
        val current_idf_char = code_ext(idf_current_idx)

        if (is_digit(current_idf_char))
            get_idx_integer_end_rec(code_ext, idf_current_idx + 1)
        else idf_current_idx
    }

    private def lexx_integer(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(is_digit(code_ext(current_idx)))

        val idx_idf_start = current_idx
        val idx_idf_end = get_idx_integer_end_rec(code_ext, idx_idf_start + 1)

        (code_ext, idx_idf_end, tokens.appended(TokenLitBint(
            code_ext.substring(idx_idf_start, idx_idf_end)
        )))
    }

    private def lexx_paren_open(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext(current_idx) == '(')
        (code_ext, current_idx + 1, tokens.appended(TokenParenOpen))
    }

    private def lexx_paren_close(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext(current_idx) == ')')
        (code_ext, current_idx + 1, tokens.appended(TokenParenClose))
    }

    private def lexx_eq_gr(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("=>"))
        (code_ext, current_idx + 2, tokens.appended(TokenEqGr))
    }

    private def lexx_less_minus(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("<-"))
        (code_ext, current_idx + 2, tokens.appended(TokenLessMinus))
    }

    private def lexx_eq(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("="))
        rt_assert(!is_operator_char(code_ext(current_idx + 1)))
        (code_ext, current_idx + 1, tokens.appended(TokenEq))
    }

    private def lexx_endl(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext.substring(current_idx).startsWith("\n"))
        (code_ext, current_idx + 1, tokens.appended(TokenEndl))
    }

    private def lexx_dot(code_ext: String, current_idx: Int, tokens: List[Token]) = {
        rt_assert(code_ext(current_idx) == '.')
        (code_ext, current_idx + 1, tokens.appended(TokenDot))
    }

    private def lexx_string(code_ext: String, token_idx_end: Int, tokens: List[Token]) = {
        rt_assert(code_ext(token_idx_end) == '\"')
        val idx_string_start = token_idx_end + 1
        val idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
        (code_ext, idx_string_end + 1, tokens.appended(TokenLitStr(
            code_ext.substring(idx_string_start, idx_string_end)
        )))
    }

    private def lexx_operator(code_ext: String, token_idx_end: Int, tokens: List[Token]) = {
        rt_assert(is_operator_char(code_ext(token_idx_end)))
        val idx_operator_start = token_idx_end
        val idx_operator_end = get_idx_operator_end_rec(code_ext, idx_operator_start)
        (code_ext, idx_operator_end, tokens.appended(TokenIdf(
            code_ext.substring(idx_operator_start, idx_operator_end)
        )))
    }

    private type LexxBundle = (String, Int, List[Token])
    private type Tokenizer = LexxBundle => LexxBundle

    //@tailrec
    private def tokenize_first_of(
        code_ext: String,
        current_idx: Int,
        tokens: List[Token],
        tokenizers: List[Tokenizer]
    ): LexxBundle = {
        def try_next_tokenizer(
            current_tokenizer: Tokenizer,
            rest_tokenizers: List[Tokenizer]
        ) =
            try_and_match(
                () => current_tokenizer(code_ext, current_idx, tokens),
                identity,
                () => tokenize_first_of(
                    code_ext, current_idx, tokens, rest_tokenizers
                )
            )

        match_list(
            case_empty = Some(() => rtFail(
                "Can't tokenize.",
                s"Given `$current_idx` `$code_ext`.",
            )),
            case_at_least_1 = Some((head, tail) =>
                try_next_tokenizer(head, tail)
            ),
        )(tokenizers)
    }

    //@tailrec
    private def tokenize_rec(
        code_ext: String,
        current_idx: Int,
        tokens: List[Token],
    ): List[Token] = {
        val current_char = code_ext(current_idx)

        if (current_char == end_of_code)
            tokens
        else if (current_char == ' ')
            tokenize_rec(code_ext, current_idx + 1, tokens)
        else
            tokenize_rec.tupled(tokenize_first_of(
                code_ext, current_idx, tokens, all_tokenizers
            ))
    }


    def tokenize(code: String): List[Token] =
        tokenize_rec(code + end_of_code, 0, List())
}
