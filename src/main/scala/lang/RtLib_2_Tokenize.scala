package lang

object RtLib_2_Tokenize {
    sealed trait Token

    final case class TokenLitStr(s: String) extends Token:
        def repr =
            s"""TokenLitStr("$s")"""

    final case class TokenLitBint(i: Int) extends Token:
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
    ): Token => A = {
        def matcher(value: Token) = {
            def call_or_otherwise(funcCalled: Option[A]) =
                funcCalled
                    .orElse(otherwise)
                    .getOrElse(rtFail(s"Unspecified case for `${value}` of type `${type value}`."))

            //    return ({
            //        TokenLitStr: lambda
            //        : call_or_otherwise(case_lit_str,
            //        val.s
            //        ),
            //        TokenLitBint: lambda
            //        : call_or_otherwise(case_lit_bint,
            //        val.i
            //        ),
            //        TokenIdf: lambda
            //        : call_or_otherwise(case_idf,
            //        val.s
            //        ),
            //        TokenParenOpen: lambda
            //        : call_or_otherwise(case_paren_open)
            //        ,
            //        TokenParenClose: lambda
            //        : call_or_otherwise(case_paren_close)
            //        ,
            //        TokenLessMinus: lambda
            //        : call_or_otherwise(case_less_minus)
            //        ,
            //        TokenEq: lambda
            //        : call_or_otherwise(case_eq)
            //        ,
            //        TokenEndl: lambda
            //        : call_or_otherwise(case_endl)
            //        ,
            //        TokenEqGr: lambda
            //        : call_or_otherwise(case_eq_gr)
            //        ,
            //        TokenDot: lambda
            //        : call_or_otherwise(case_dot)
            //        ,
            //    }
            //        .get(
            //    type (
            //    val),
            //    lambda: fail
            //    (f"Value {val} {type(val)} is not a Token.")
            //    ) ) ()
            //
            //    return matcher
            ???
        }
    }
