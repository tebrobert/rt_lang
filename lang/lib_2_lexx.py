from lang.lib_1_desugar import *


class TokenLitStr:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f"""TokenLitStr("{self.s}")"""


class TokenIdf:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f"""TokenIdf("{self.s}")"""


class TokenParenOpen:
    def __repr__(self):
        return "TokenParenOpen()"


class TokenParenClose:
    def __repr__(self):
        return "TokenParenClose()"


class TokenEqGr:
    def __repr__(self):
        return "TokenEqGr()"


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


def lexx_idf(code_ext, tokens, token_idx_end):
    idx_idf_start = token_idx_end
    idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)
    return (code_ext, idx_idf_end,
    tokens + [TokenIdf(code_ext[idx_idf_start:idx_idf_end])]
    )


def lexx_eq_gr(code_ext, tokens, token_idx_end):
    return ((code_ext, token_idx_end + 2, tokens + [TokenEqGr()])
            if code_ext[token_idx_end + 1] == ">"
            else fail_bad_eq_seq(code_ext, token_idx_end)
            )


def lexx_string(code_ext, tokens, token_idx_end):
    idx_string_start = token_idx_end + 1
    idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
    return (code_ext, idx_string_end + 1,
    tokens + [TokenLitStr(code_ext[idx_string_start:idx_string_end])],
    )


def lexx_operator(code_ext, tokens, token_idx_end):
    idx_operator_start = token_idx_end
    idx_operator_end = get_idx_operator_end_rec(code_ext, idx_operator_start)
    return (code_ext, idx_operator_end,
    tokens + [TokenIdf(code_ext[idx_operator_start:idx_operator_end])],
    )


@tailrec
def lexx_rec(code_ext, current_idx, tokens):
    current_char = code_ext[current_idx]
    return (tokens if current_char == end_of_code else
            rec(*lexx_idf(code_ext, tokens, current_idx))
            if is_initial_idf_char(current_char) else
            rec(code_ext, current_idx + 1, tokens + [TokenParenOpen()])
            if current_char == "(" else
            rec(code_ext, current_idx + 1, tokens + [TokenParenClose()])
            if current_char == ")" else
            rec(*lexx_eq_gr(code_ext, tokens, current_idx))
            if current_char == "=" else  # other than `=>` sequences exist
            rec(code_ext, current_idx + 1, tokens)
            if current_char == " " else
            rec(*lexx_string(code_ext, tokens, current_idx))
            if current_char == "\"" else
            rec(*lexx_operator(code_ext, tokens, current_idx))
            if is_operator_char(current_char) else
            fail(f"""Unexpected current_char "{current_char}".""")
            )


def lexx(code):
    return lexx_rec(code + end_of_code, 0, [])


end_of_code = "\0"
