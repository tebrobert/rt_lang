from lang.lib_1_desugar import *


class TokenLitStr:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Token_Lit_Str("{self.s}")'


class TokenIdf:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Token_Idf("{self.s}")'


class TokenParenOpen:
    def __repr__(self):
        return 'Token_Paren_Open()'


class TokenParenClose:
    def __repr__(self):
        return 'Token_Paren_Close()'


class TokenEqGr:
    def __repr__(self):
        return 'Token_Eq_Gr()'


@tailrec
def get_idx_string_end_rec(code_ext, idx_string_end):
    current_string_char = code_ext[idx_string_end]
    return (
        idx_string_end
        if current_string_char == '"' else
        fail(f"No closing `\"` for string literal.")
        if current_string_char == end_of_code else
        rec(code_ext, idx_string_end + 1)
    )


@tailrec
def get_idx_idf_end_rec(code_ext, idf_current_idx):
    current_idf_char = code_ext[idf_current_idx]
    return (rec(code_ext, idf_current_idx + 1)
            if ('a' <= current_idf_char <= 'z'
                or 'A' <= current_idf_char <= 'Z'
                or '0' <= current_idf_char <= '9'
                or current_idf_char == '_'
                )
            else idf_current_idx
            )


def lexx_idf(code_ext, tokens, token_idx_end):
    idx_idf_start = token_idx_end
    idx_idf_end = get_idx_idf_end_rec(code_ext, idx_idf_start + 1)
    return (code_ext, idx_idf_end,
    tokens + [TokenIdf(code_ext[idx_idf_start:idx_idf_end])]
    )


def lexx_eq_gr(code_ext, tokens, token_idx_end):
    return ((code_ext, token_idx_end + 2, tokens + [TokenEqGr()])
            if code_ext[token_idx_end + 1] == '>'
            else fail(
        f'Unexpected sequence "={code_ext[token_idx_end + 1]}"'
    )
            )


def lexx_string(code_ext, tokens, token_idx_end):
    idx_string_start = token_idx_end + 1
    idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
    return (code_ext, idx_string_end + 1,
    tokens + [TokenLitStr(code_ext[idx_string_start:idx_string_end])]
    )


@tailrec
def lexx_rec(code_ext, token_idx_end, tokens):
    current_char = code_ext[token_idx_end]
    return (
        tokens
        if current_char == end_of_code else
        rec(*lexx_idf(code_ext, tokens, token_idx_end))
        if ('a' <= current_char <= 'z'
            or 'A' <= current_char <= 'Z'
            or current_char == "_"
            ) else
        rec(code_ext, token_idx_end + 1, tokens + [TokenParenOpen()])
        if current_char == '(' else
        rec(code_ext, token_idx_end + 1, tokens + [TokenParenClose()])
        if current_char == ')' else
        rec(*lexx_eq_gr(code_ext, tokens, token_idx_end))
        if current_char == '=' else
        rec(code_ext, token_idx_end + 1, tokens)
        if current_char == ' ' else
        rec(*lexx_string(code_ext, tokens, token_idx_end))
        if current_char == '"' else
        fail(f'Unexpected current_char "{current_char}"')
    )


def lexx(code):
    return lexx_rec(code + end_of_code, 0, [])


end_of_code = '\0'
