from desugar import *

class LexxErr(ValueError):
    def __init__(self, msg):
        self.msg = f"LexxErr: {msg}"

class Token_Lit_Str:
    def __init__(self, s): self.s = s
    def __repr__(self): return f'Token_Lit_Str("{self.s}")'

class Token_Idf:
    def __init__(self, s): self.s = s
    def __repr__(self): return f'Token_Idf("{self.s}")'

class Token_Paren_Open:
    def __repr__(self): return 'Token_Paren_Open()'

class Token_Paren_Close:
    def __repr__(self): return 'Token_Paren_Close()'

class Token_Eq_Gr:
    def __repr__(self): return 'Token_Eq_Gr()'

def lexx(code):
    end_of_code = '\0'
    code += end_of_code
    a = 0
    b = 0
    tokens = []
    char = code[b]

    while char != end_of_code:
        if 'a' <= char <= 'z' or 'A' <= char <= 'Z' or char == "_":
            a = b
            while True:
                b += 1
                char = code[b]
                if not('a' <= char <= 'z' or 'A' <= char <= 'Z' or '0' <= char <= '9' or char == '_'):
                    break

            tokens.append(Token_Idf(code[a:b]))
    
        elif char == '(':
            tokens.append(Token_Paren_Open())
            b += 1

        elif char == ')':
            tokens.append(Token_Paren_Close())
            b += 1

        elif char == ')':
            tokens.append(Token_Paren_Close())
            b += 1

        elif char == '=':
            char = code[b + 1]
            if char == '>':
                tokens.append(Token_Eq_Gr())
                b += 2

            else : raise LexxErr(f'Unexpected sequence "={char}"')

        elif char == ' ':
            b += 1

        elif char == '"':
            b += 1
            a = b
            char = code[b]
            while char != '"':
                if char == end_of_code:
                    raise LexxErr(f"No closing `\"` for string literal.")
                b += 1
                char = code[b]
            tokens.append(Token_Lit_Str(code[a:b]))
            b += 1

        else: raise LexxErr(f'Unexpected char "{char}"')

        char = code[b]

    return tokens
