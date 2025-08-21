package org.rt.lang

import org.rt.lang.RtLib_2_Tokenize.Classes.*
import org.rt.utils.RtFail.{rtFail, rt_assert, tryOrRecover}
import org.rt.utils.RtList.rtMatch

import scala.annotation.tailrec

object RtLib_2_Tokenize {
  object Classes {
    sealed trait Tok // token

    final case class TokLitStr(s: String) extends Tok // literal string

    final case class TokLitBint(i: String) extends Tok //literal big int

    final case class TokIdf(s: String) extends Tok // identifier

    case object TokParenOpen extends Tok // `(`

    case object TokParenClose extends Tok // `)`

    case object TokLessMinus extends Tok // `<-`

    case object TokEq extends Tok // `=`

    case object TokEndl extends Tok // `\n`

    case object TokEqGr extends Tok // `=>`

    case object TokDot extends Tok // `.`
  }

  private object Internal:
    val end_of_code: Char = 0

    val char_to_latin =
      Map(
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

    val all_tokenizers: List[Tokenizer] =
      List(
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

    def unexpectedToken(token: Tok) =
      rtFail(s"Unspecified case for `$token` of type `${type value}`.")

    def match_token[A](
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
    ): Tok => A =
      (token: Tok) => {
        def call_or_otherwise(funcCalled: Option[() => A]): A =
          funcCalled
            .orElse(otherwise)
            .getOrElse(rtFail(
              s"Unspecified case for `$token` of type `${type value}`.",
            ))
            ()

        call_or_otherwise(token match
          case TokLitStr(s) => case_lit_str.map(f => () => f(s))
          case TokLitBint(i) => case_lit_bint.map(f => () => f(i))
          case TokIdf(s) => case_idf.map(f => () => f(s))
          case TokParenOpen => case_paren_open
          case TokParenClose => case_paren_close
          case TokLessMinus => case_less_minus
          case TokEq => case_eq
          case TokEndl => case_endl
          case TokEqGr => case_eq_gr
          case TokDot => case_dot
        )
      }

    def rt_assert_token_idf(
      token: Tok,
    ) =
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
      char_to_latin.keys.toSeq.contains(char.toString)

    @tailrec
    def get_idx_string_end_rec(code_ext: String, idx_string_end: Int): Int = {
      val current_string_char = code_ext(idx_string_end)

      if (current_string_char == '\"')
        idx_string_end
      else if (current_string_char == end_of_code)
        rtFail(f"""No closing `"` for string literal.""")
      else get_idx_string_end_rec(code_ext, idx_string_end + 1)
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

    def lexx_idf(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(is_initial_idf_char(code_ext(current_idx)))

      val idx_idf_start = current_idx
      val idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)

      (code_ext, idx_idf_end, tokens.appended(TokIdf(
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

    def lexx_integer(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(is_digit(code_ext(current_idx)))

      val idx_idf_start = current_idx
      val idx_idf_end = get_idx_integer_end_rec(code_ext, idx_idf_start + 1)

      (code_ext, idx_idf_end, tokens.appended(TokLitBint(
        code_ext.substring(idx_idf_start, idx_idf_end)
      )))
    }

    def lexx_paren_open(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext(current_idx) == '(')
      (code_ext, current_idx + 1, tokens.appended(TokParenOpen))
    }

    def lexx_paren_close(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext(current_idx) == ')')
      (code_ext, current_idx + 1, tokens.appended(TokParenClose))
    }

    def lexx_eq_gr(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext.substring(current_idx).startsWith("=>"))
      (code_ext, current_idx + 2, tokens.appended(TokEqGr))
    }

    def lexx_less_minus(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext.substring(current_idx).startsWith("<-"))
      (code_ext, current_idx + 2, tokens.appended(TokLessMinus))
    }

    def lexx_eq(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext.substring(current_idx).startsWith("="))
      rt_assert(!is_operator_char(code_ext(current_idx + 1)))
      (code_ext, current_idx + 1, tokens.appended(TokEq))
    }

    def lexx_endl(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext.substring(current_idx).startsWith("\n"))
      (code_ext, current_idx + 1, tokens.appended(TokEndl))
    }

    def lexx_dot(code_ext: String, current_idx: Int, tokens: List[Tok]) = {
      rt_assert(code_ext(current_idx) == '.')
      (code_ext, current_idx + 1, tokens.appended(TokDot))
    }

    def lexx_string(code_ext: String, token_idx_end: Int, tokens: List[Tok]) = {
      rt_assert(code_ext(token_idx_end) == '\"')
      val idx_string_start = token_idx_end + 1
      val idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
      (code_ext, idx_string_end + 1, tokens.appended(TokLitStr(
        code_ext.substring(idx_string_start, idx_string_end)
      )))
    }

    def lexx_operator(code_ext: String, token_idx_end: Int, tokens: List[Tok]) = {
      rt_assert(is_operator_char(code_ext(token_idx_end)))
      val idx_operator_start = token_idx_end
      val idx_operator_end = get_idx_operator_end_rec(code_ext, idx_operator_start)
      (code_ext, idx_operator_end, tokens.appended(TokIdf(
        code_ext.substring(idx_operator_start, idx_operator_end)
      )))
    }

    type LexxBundle = (String, Int, List[Tok])
    type Tokenizer = LexxBundle => LexxBundle

    inline
    def tryNextTokenizer(
      inline lexxBundle: LexxBundle,
    )(
      inline current_tokenizer: Tokenizer,
      inline rest_tokenizers: List[Tokenizer],
    ): LexxBundle =
      tryOrRecover(
        () => current_tokenizer(lexxBundle),
        () => tokenize_first_of(lexxBundle)(rest_tokenizers),
      )

    @tailrec
    def tokenize_first_of(
      lexxBundle: LexxBundle,
    )(
      tokenizers: List[Tokenizer],
    ): LexxBundle =
      tokenizers.rtMatch(
        caseEmpty = () => rtFail("Can't tokenize.", s"Given `$lexxBundle`."),
        caseAtLeast1 = tryNextTokenizer(lexxBundle),
      )

    @tailrec
    def tokenize_rec(
      lexxBundle: LexxBundle,
    ): List[Tok] = {
      val (code_ext, current_idx, tokens) = lexxBundle
      val current_char = code_ext(current_idx)

      if (current_char == end_of_code)
        tokens
      else if (current_char == ' ')
        tokenize_rec((code_ext, current_idx + 1, tokens))
      else tokenize_rec(tokenize_first_of(lexxBundle)(all_tokenizers))
    }
  end Internal

  def tokenize(code: String): List[Tok] =
    Internal.tokenize_rec((code + Internal.end_of_code, 0, List()))
}
