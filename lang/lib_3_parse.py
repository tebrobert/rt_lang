from lang.lib_2_lexx import *

class ParseErr(ValueError):
    def __init__(self, msg):
        self.msg = f"ParseErr: {msg}"

    def __repr__(self):
        return self.msg

class Expr_Lit_Str:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f'{indent}Expr_Lit_Str("{self.s}")'

class Expr_Idf:
    def __init__(self, s):
        self.s = s

    def __repr__(self, indent=''):
        return f'{indent}Expr_Idf("{self.s}")'

class Expr_Call_1:
    def __init__(self, expr_f, expr_x):
        self.expr_f, self.expr_x = expr_f, expr_x

    def __repr__(self, indent=''):
        return (f'{indent}Expr_Call_1(\n'
            + f'{self.expr_f.__repr__(indent + 4*" ")},\n'
            + f'{self.expr_x.__repr__(indent + 4*" ")}\n'
            + f'{indent})'
        )

class Expr_Lambda_1:
    def __init__(self, eidf_x, expr_res):
        if type(eidf_x) is not Expr_Idf:
            fail(ParseErr(f'Expr_Idf expected as the first arg of Expr_Lambda_1'))

        self.eidf_x, self.expr_res = eidf_x, expr_res

    def __repr__(self, indent=''):
        return (f'{indent}Expr_Lambda_1(\n'
            + f'{self.eidf_x.__repr__(indent + 4*" ")},\n'
            + f'{self.expr_res.__repr__(indent + 4*" ")}\n'
            + f'{indent})'
        )

def parse_lit_str(tokens, i):
    if type(tokens[i]) is not Token_Lit_Str:
        return fail(ParseErr(f'Token_Lit_Str expected given {i} {tokens}'))

    return Expr_Lit_Str(tokens[i].s), i + 1

def parseIdf(tokens, i):
    if type(tokens[i]) is not Token_Idf:
        return fail(ParseErr(f'Token_Idf expected given {i} {tokens}'))

    return Expr_Idf(tokens[i].s), i + 1

def parseBraced(tokens, i):
    if type(tokens[i]) is not Token_Paren_Open:
        return fail(ParseErr(f'Token_Paren_Open: expected given {i} {tokens}'))

    expr, j = parseExpr(tokens, i + 1)

    if type(tokens[j]) is not Token_Paren_Close:
        return fail(ParseErr(f'Token_Paren_Open expected at {j} given {i} {tokens}'))

    return expr, j + 1

def parseLambda1(tokens, i):
    eidf_x, j = parseIdf(tokens, i)

    if type(tokens[j]) is not Token_Eq_Gr:
        return fail(ParseErr(f'Token_Eq_Gr expected at {j} given {i} {tokens}'))

    expr_res, k = parseExpr(tokens, j + 1)
    return Expr_Lambda_1(eidf_x, expr_res), k

@tailrec
def parseCall(tokens, expr_f, i):
    if type(tokens[i]) is not Token_Paren_Open:
        return fail(ParseErr(f'Token_Paren_Open: expected, given {i} {tokens}'))

    if type(tokens[i+1]) is Token_Paren_Close:
        return fail(ParseErr(f'Remove deprecated empty parenthesis, given {i} {tokens}'))

    expr_x, j = parseExpr(tokens, i + 1)

    if type(tokens[j]) is not Token_Paren_Close:
        return fail(ParseErr(f'Token_Paren_Open expected at {j} given {i} {tokens}'))

    k = j + 1
    expr_call_1 = Expr_Call_1(expr_f, expr_x)

    if type(tokens[k]) is Token_Paren_Open:
        return rec(tokens, expr_call_1, k)

    return expr_call_1, k

@tailrec
def parseAnyOf(tokens, i, parsers):
    if parsers == []:
        return fail(ParseErr(f'Can\'t parse Expr given {i} {tokens}'))

    try:
        expr, j = parsers[0](tokens, i)

    except ParseErr:
        return rec(tokens, i, parsers[1:])

    if type(tokens[j]) is Token_Paren_Open:
        return parseCall(tokens, expr, j)

    return expr, j

def parseExpr(tokens, i):
    return parseAnyOf(tokens, i, [parseLambda1, parseIdf, parseBraced, parse_lit_str])

def parse(tokens):
    end_of_tokens = '\0'
    ext_tokens = tokens + [end_of_tokens]
    expr, i = parseExpr(ext_tokens, 0)

    if ext_tokens[i] != end_of_tokens:
        return fail(ParseErr(f'Unexpected token at {i} given {tokens}'))

    return expr
