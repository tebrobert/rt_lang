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
        if 'a' <= char <= 'z':
            a = b
            while True:
                b += 1
                char = code[b]
                if not('a' <= char <= 'z' or '0' <= char <= '9' or char == '_'):
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

            else : raise Exception(f'Unexpected sequence "={char}"')

        elif char == ' ':
            b += 1

        else: raise Exception(f'Unexpected char "{char}"')

        char = code[b]

    return tokens
