from lang.lib_1_desugar import *


class TokenLitStr:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f"""TokenLitStr("{self.s}")"""

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class TokenIdf:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f"""TokenIdf("{self.s}")"""

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class TokenParenOpen:
    def __repr__(self):
        return "TokenParenOpen()"

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class TokenParenClose:
    def __repr__(self):
        return "TokenParenClose()"

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class TokenEqGr:
    def __repr__(self):
        return "TokenEqGr()"

    def __eq__(self, that):
        return f"{self}" == f"{that}"


class TokenDot:
    def __repr__(self):
        return "TokenDot()"

    def __eq__(self, that):
        return f"{self}" == f"{that}"


def is_initial_idf_char(char):
    return (char == "_"
            or "a" <= char <= "z"
            or "A" <= char <= "Z"
            )


def is_non_initial_idf_char(char):
    return (is_initial_idf_char(char)
            or "0" <= char <= "9"
            )


def is_operator_char(char):
    return char in ["+", "-", "*", "/", "%", ">", "<", "=", "!"]


def fail_bad_eq_seq(code_ext, token_idx_end):
    fail(f"""Unexpected sequence "={code_ext[token_idx_end + 1]}".""")


@tailrec
def get_idx_string_end_rec(code_ext, idx_string_end):
    current_string_char = code_ext[idx_string_end]
    return (idx_string_end
            if current_string_char == "\"" else
            fail(f"""No closing `"` for string literal.""")
            if current_string_char == end_of_code else
            rec(code_ext, idx_string_end + 1)
            )


@tailrec
def get_idx_idf_end_rec(code_ext, idf_current_idx):
    current_idf_char = code_ext[idf_current_idx]
    return (rec(code_ext, idf_current_idx + 1)
            if is_non_initial_idf_char(current_idf_char)
            else idf_current_idx
            )


@tailrec
def get_idx_operator_end_rec(code_ext, operator_current_idx):
    current_operator_char = code_ext[operator_current_idx]
    return (rec(code_ext, operator_current_idx + 1)
            if is_operator_char(current_operator_char)
            else operator_current_idx
            )


def lexx_idf(code_ext, current_idx, tokens):
    rt_assert(is_initial_idf_char(code_ext[current_idx]))
    idx_idf_start = current_idx
    idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)
    return (code_ext, idx_idf_end,
    tokens + [TokenIdf(code_ext[idx_idf_start:idx_idf_end])]
    )


def lexx_paren_open(code_ext, current_idx, tokens):
    rt_assert(code_ext[current_idx] == "(")
    return (code_ext, current_idx + 1, tokens + [TokenParenOpen()])


def lexx_paren_close(code_ext, current_idx, tokens):
    rt_assert(code_ext[current_idx] == ")")
    return (code_ext, current_idx + 1, tokens + [TokenParenClose()])


def lexx_eq_gr(code_ext, current_idx, tokens):
    rt_assert(code_ext[current_idx:].startswith("=>"))
    return (code_ext, current_idx + 2, tokens + [TokenEqGr()])


def lexx_dot(code_ext, current_idx, tokens):
    rt_assert(code_ext[current_idx] == ".")
    return (code_ext, current_idx + 1, tokens + [TokenDot()])


def lexx_string(code_ext, token_idx_end, tokens):
    rt_assert(code_ext[token_idx_end] == "\"")
    idx_string_start = token_idx_end + 1
    idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
    return (code_ext, idx_string_end + 1,
    tokens + [TokenLitStr(code_ext[idx_string_start:idx_string_end])],
    )


def lexx_operator(code_ext, token_idx_end, tokens):
    rt_assert(is_operator_char(code_ext[token_idx_end]))
    idx_operator_start = token_idx_end
    idx_operator_end = get_idx_operator_end_rec(code_ext, idx_operator_start)
    return (code_ext, idx_operator_end,
    tokens + [TokenIdf(code_ext[idx_operator_start:idx_operator_end])],
    )


@tailrec
def lexx_first_of(code_ext, current_idx, tokens, lexxers):
    fail_if(lexxers == [],
        f"Can't lexx. Given `{current_idx}` `{code_ext}`."
    )
    either_result = rt_try(lambda: lexxers[0](code_ext, current_idx, tokens))
    return (
        rec(code_ext, current_idx, tokens, lexxers[1:])
        if is_fail(either_result) else
        either_result
    )


@tailrec
def lexx_rec(code_ext, current_idx, tokens):
    current_char = code_ext[current_idx]
    return (
        tokens if current_char == end_of_code else
        rec(code_ext, current_idx + 1, tokens)
        if current_char == " " else
        rec(*lexx_first_of(code_ext, current_idx, tokens, [
            lexx_idf,
            lexx_paren_open,
            lexx_paren_close,
            lexx_eq_gr,
            lexx_string,
            lexx_operator,
            lexx_dot,
        ]))
    )


def tokenize(code):
    return lexx_rec(code + end_of_code, 0, [])


end_of_code = "\0"