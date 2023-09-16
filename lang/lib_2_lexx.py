from lang.lib_1_desugar import *


class LexxErr(ValueError):
    def __init__(self, msg):
        self.msg = f"LexxErr: {msg}"

    def __repr__(self):
        return self.msg


class Token_Lit_Str:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Token_Lit_Str("{self.s}")'


class Token_Idf:
    def __init__(self, s):
        self.s = s

    def __repr__(self):
        return f'Token_Idf("{self.s}")'


class Token_Paren_Open:
    def __repr__(self):
        return 'Token_Paren_Open()'


class Token_Paren_Close:
    def __repr__(self):
        return 'Token_Paren_Close()'


class Token_Eq_Gr:
    def __repr__(self):
        return 'Token_Eq_Gr()'


end_of_code = '\0'


@tailrec
def get_idx_string_end_rec(code_ext, idx_string_end):
    current_string_char = code_ext[idx_string_end]
    return (
        idx_string_end
        if current_string_char == '"' else
        fail(LexxErr(f"No closing `\"` for string literal."))
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
            tokens + [Token_Idf(code_ext[idx_idf_start:idx_idf_end])]
            )


def lexx_eq_gr(code_ext, tokens, token_idx_end):
    return ((code_ext, token_idx_end + 2, tokens + [Token_Eq_Gr()])
            if code_ext[token_idx_end + 1] == '>'
            else fail(LexxErr(
        f'Unexpected sequence "={code_ext[token_idx_end + 1]}"'
    ))
            )


def lexx_string(code_ext, tokens, token_idx_end):
    idx_string_start = token_idx_end + 1
    idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
    return (code_ext, idx_string_end + 1,
            tokens + [Token_Lit_Str(code_ext[idx_string_start:idx_string_end])]
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
        rec(code_ext, token_idx_end + 1, tokens + [Token_Paren_Open()])
        if current_char == '(' else
        rec(code_ext, token_idx_end + 1, tokens + [Token_Paren_Close()])
        if current_char == ')' else
        rec(*lexx_eq_gr(code_ext, tokens, token_idx_end))
        if current_char == '=' else
        rec(code_ext, token_idx_end + 1, tokens)
        if current_char == ' ' else
        rec(*lexx_string(code_ext, tokens, token_idx_end))
        if current_char == '"' else
        fail(LexxErr(f'Unexpected current_char "{current_char}"'))
    )


def lexx(code):
    return lexx_rec(code + end_of_code, 0, [])
