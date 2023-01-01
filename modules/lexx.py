from modules.tailrec import *
from modules.desugar import *

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

    if current_string_char == '"':
        return idx_string_end

    if current_string_char == end_of_code:
        raise LexxErr(f"No closing `\"` for string literal.")

    return rec(code_ext, idx_string_end + 1)

@tailrec
def get_idx_idf_end_rec(code_ext, idf_current_idx):
    current_idf_char = code_ext[idf_current_idx]

    if not('a' <= current_idf_char <= 'z'
        or 'A' <= current_idf_char <= 'Z'
        or '0' <= current_idf_char <= '9'
        or current_idf_char == '_'
    ):
        return idf_current_idx

    return rec(
        code_ext = code_ext,
        idf_current_idx = idf_current_idx + 1
    )

@tailrec
def lexx_rec(code_ext, token_idx_end, tokens):
    current_char = code_ext[token_idx_end]

    if current_char == end_of_code:
        return tokens

    if 'a' <= current_char <= 'z' or 'A' <= current_char <= 'Z' or current_char == "_":
        idx_idf_start = token_idx_end
        idx_idf_end = get_idx_idf_end_rec(
            code_ext = code_ext,
            idf_current_idx = idx_idf_start + 1
        )

        return rec(
            code_ext = code_ext,
            token_idx_end = idx_idf_end,
            tokens = tokens + [Token_Idf(code_ext[idx_idf_start:idx_idf_end])]
        )

    elif current_char == '(':
        return rec(
            code_ext = code_ext,
            token_idx_end = token_idx_end + 1,
            tokens = tokens + [Token_Paren_Open()]
        )

    elif current_char == ')':
        return rec(
            code_ext = code_ext,
            token_idx_end = token_idx_end + 1,
            tokens = tokens + [Token_Paren_Close()]
        )

    elif current_char == '=':
        next_char = code_ext[token_idx_end + 1]
        if next_char == '>':
            return rec(
                code_ext = code_ext,
                token_idx_end = token_idx_end + 2,
                tokens = tokens + [Token_Eq_Gr()]
            )

        else:
            raise LexxErr(f'Unexpected sequence "={next_char}"')

    elif current_char == ' ':
        return rec(
            code_ext = code_ext,
            token_idx_end = token_idx_end + 1,
            tokens = tokens
        )

    elif current_char == '"':
        idx_string_start = token_idx_end + 1
        idx_string_end = get_idx_string_end_rec(code_ext, idx_string_start)
        
        return rec(
            code_ext = code_ext,
            token_idx_end = idx_string_end + 1,
            tokens = tokens + [Token_Lit_Str(code_ext[idx_string_start:idx_string_end])]
        )

    else:
        raise LexxErr(f'Unexpected current_char "{current_char}"')

def lexx(code):
    return lexx_rec(
        code_ext = code + end_of_code,
        token_idx_end = 0,
        tokens = []
    )
