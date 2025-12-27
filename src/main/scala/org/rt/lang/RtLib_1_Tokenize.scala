package org.rt.lang

import org.rt.lang.RtLib_1_Tokenize.Public.*
import org.rt.utils.RtFail.{
  RtFail,
  rtAssert,
  rtAssertUnsafe,
  rtFail,
  tryOrRecoverUnsafe,
}
import org.rt.utils.RtList.rtMatch

import scala.annotation.tailrec

object RtLib_1_Tokenize {
  object Public {
    sealed trait Tok // token
    case class TokLitStr(s: String) extends Tok // literal string
    case class TokLitBint(i: String) extends Tok // literal big int
    case class TokIdf(s: String) extends Tok // identifier
    case object TokParenOpen extends Tok // `(`
    case object TokParenClose extends Tok // `)`
    case object TokLessMinus extends Tok // `<-`
    case object TokEq extends Tok // `=`
    case object TokEndl extends Tok // `\n`
    case object TokEqGr extends Tok // `=>`
    case object TokDot extends Tok // `.`

    def tokenize(code: String): Either[RtFail, List[Tok]] =
      Internal.tokenize_rec(Right(code + Internal.end_of_code, 0, List()))
  }

  private object Internal {
    val end_of_code: Char = '\u0000'

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

//    def call_or_otherwise[A](
//      otherwise: Option[() => A],
//      token: Tok,
//    )(
//      funcCalled: Option[() => A],
//    ): Either[RtFail, A] =
//      (funcCalled orElse otherwise)
//        .toRight(RtFail(
//          s"Unspecified case for `$token` of type `${type value}`.",
//        ))
//        .map(_())

    def is_initial_idf_char(
        char: Char,
    ) =
      char == '_'
        || 'a' <= char && char <= 'z'
        || 'A' <= char && char <= 'Z'

    def is_digit(
        char: Char,
    ) =
      '0' <= char && char <= '9'

    def is_non_initial_idf_char(
        char: Char,
    ) =
      is_initial_idf_char(char)
        || is_digit(char)

    def is_operator_char(
        char: Char,
    ) =
      char_to_latin.keys.toSeq.contains(char.toString)

    @tailrec
    def get_idx_string_end_rec(
        code_ext: String,
        idx_string_end: Int,
    ): Either[RtFail, Int] = {
      val current_string_char = code_ext(idx_string_end)

      if (current_string_char == '\"')
        Right(idx_string_end)
      else if (current_string_char == end_of_code)
        rtFail(f"""No closing `"` for string literal.""")
      else get_idx_string_end_rec(code_ext, idx_string_end + 1)
    }

    @tailrec
    def get_idx_idf_end_rec(
        code_ext: String,
        idf_current_idx: Int,
    ): Int = {
      val current_idf_char = code_ext(idf_current_idx)

      if (is_non_initial_idf_char(current_idf_char))
        get_idx_idf_end_rec(code_ext, idf_current_idx + 1)
      else idf_current_idx
    }

    @tailrec
    def get_idx_operator_end_rec(
        code_ext: String,
        operator_current_idx: Int,
    ): Int = {
      val current_operator_char = code_ext(operator_current_idx)

      if (is_operator_char(current_operator_char))
        get_idx_operator_end_rec(code_ext, operator_current_idx + 1)
      else operator_current_idx
    }

    def lexx_idf(
        code_ext: String,
        current_idx: Int,
        tokens: List[Tok],
    ) = {
      for {
        _ <- rtAssert(is_initial_idf_char(code_ext(current_idx)))
        idx_idf_start = current_idx
        idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)
      } yield (
        code_ext,
        idx_idf_end,
        tokens.appended(
          TokIdf(
            code_ext.substring(idx_idf_start, idx_idf_end),
          ),
        ),
      )
    }

    @tailrec
    def get_idx_integer_end_rec(
        code_ext: String,
        idf_current_idx: Int,
    ): Int = {
      val current_idf_char = code_ext(idf_current_idx)

      if (is_digit(current_idf_char))
        get_idx_integer_end_rec(code_ext, idf_current_idx + 1)
      else idf_current_idx
    }

    def lexx_integer(
        code_ext: String,
        current_idx: Int,
        tokens: List[Tok],
    ) =
      for {
        _ <- rtAssert(is_digit(code_ext(current_idx)))
        idx_idf_start = current_idx
        idx_idf_end = get_idx_integer_end_rec(code_ext, idx_idf_start + 1)
      } yield (
        code_ext,
        idx_idf_end,
        tokens.appended(
          TokLitBint(
            code_ext.substring(idx_idf_start, idx_idf_end),
          ),
        ),
      )

    def lexx_paren_open(
        code_ext: String,
        current_idx: Int,
        tokens: List[Tok],
    ) =
      for {
        _ <- rtAssert(code_ext(current_idx) == '(')
      } yield (code_ext, current_idx + 1, tokens.appended(TokParenOpen))

    def lexx_paren_close(
        code_ext: String,
        current_idx: Int,
        tokens: List[Tok],
    ) =
      for {
        _ <- rtAssert(code_ext(current_idx) == ')')
      } yield (code_ext, current_idx + 1, tokens.appended(TokParenClose))

    def lexx_eq_gr(code_ext: String, current_idx: Int, tokens: List[Tok]) =
      for {
        _ <- rtAssert(code_ext.substring(current_idx).startsWith("=>"))
      } yield (code_ext, current_idx + 2, tokens.appended(TokEqGr))

    def lexx_less_minus(
        code_ext: String,
        current_idx: Int,
        tokens: List[Tok],
    ) =
      for {
        _ <- rtAssert(code_ext.substring(current_idx).startsWith("<-"))
      } yield (code_ext, current_idx + 2, tokens.appended(TokLessMinus))

    def lexx_eq(code_ext: String, current_idx: Int, tokens: List[Tok]) =
      for {
        _ <- rtAssert(code_ext.substring(current_idx).startsWith("="))
        _ <- rtAssert(!is_operator_char(code_ext(current_idx + 1)))
      } yield (code_ext, current_idx + 1, tokens.appended(TokEq))

    def lexx_endl(code_ext: String, current_idx: Int, tokens: List[Tok]) =
      for {
        _ <- rtAssert(code_ext.substring(current_idx).startsWith("\n"))
      } yield (code_ext, current_idx + 1, tokens.appended(TokEndl))

    def lexx_dot(code_ext: String, current_idx: Int, tokens: List[Tok]) =
      for {
        _ <- rtAssert(code_ext(current_idx) == '.')
      } yield (code_ext, current_idx + 1, tokens.appended(TokDot))

    def lexx_string(code_ext: String, token_idx_end: Int, tokens: List[Tok]) = {
      for {
        _ <- rtAssert(code_ext(token_idx_end) == '\"')
        idx_string_start = token_idx_end + 1
        idx_string_end <- get_idx_string_end_rec(code_ext, idx_string_start)
        newToken = TokLitStr(
          code_ext.substring(idx_string_start, idx_string_end),
        )
      } yield (code_ext, idx_string_end + 1, tokens.appended(newToken))
    }

    def lexx_operator(
        code_ext: String,
        token_idx_end: Int,
        tokens: List[Tok],
    ) =
      for {
        _ <- rtAssert(is_operator_char(code_ext(token_idx_end)))
        idx_operator_start = token_idx_end
        idx_operator_end = get_idx_operator_end_rec(
          code_ext,
          idx_operator_start,
        )
        newToken = TokIdf(
          code_ext.substring(idx_operator_start, idx_operator_end),
        )
      } yield (code_ext, idx_operator_end, tokens.appended(newToken))

    type LexxBundle = (String, Int, List[Tok])
    type Tokenizer = LexxBundle => Either[RtFail, LexxBundle]

    inline def tryNextTokenizer(
        inline lexxBundle: LexxBundle,
    )(
        inline current_tokenizer: Tokenizer,
        inline rest_tokenizers: List[Tokenizer],
    ): Either[RtFail, LexxBundle] =
      current_tokenizer(lexxBundle) match {
        case Right(value) => Right(value)
        case _            => tokenize_first_of(lexxBundle)(rest_tokenizers)
      }

    @tailrec
    def tokenize_first_of(
        lexxBundle: LexxBundle,
    )(
        tokenizers: List[Tokenizer],
    ): Either[RtFail, LexxBundle] =
      tokenizers.rtMatch(
        caseEmpty = () => rtFail("Can't tokenize.", s"Given `$lexxBundle`."),
        caseAtLeast1 = tryNextTokenizer(lexxBundle),
      )

    @tailrec
    def tokenize_rec(
        eiLexxBundle: Either[RtFail, LexxBundle],
    ): Either[RtFail, List[Tok]] = {
      eiLexxBundle match {
        case Left(fail)        => Left(fail)
        case Right(lexxBundle) =>
          val (code_ext, current_idx, tokens) = lexxBundle
          val current_char = code_ext(current_idx)

          if (current_char == end_of_code)
            Right(tokens)
          else if (current_char == ' ')
            tokenize_rec(Right(code_ext, current_idx + 1, tokens))
          else tokenize_rec(tokenize_first_of(lexxBundle)(all_tokenizers))
      }
    }
  }
}
