Launch a code sample:
> python3 rt.py sample.rt

Launch a code sample and see how it really works:
> python3 rt.py sample.rt --dev

Get help:
> python3 rt.py -h



*********

EXAMPLE OUTPUT

> cat sample.rt
greeting = "Hi!"
print(greeting)
print("What is your name?")
name <- input
print("Dear ".+(name).+(", welcome!"))

> python3 rt.py sample.rt
Hi!
What is your name?
Robert
Dear Robert, welcome!

>



*********

EXAMPLE OUTPUT

> cat sample.rt
greeting = "Hi!"
print(greeting)
print("What is your name?")
name <- input
print("Dear ".+(name).+(", welcome!"))

> python3 rt.py sample.rt --dev
1_CODE
greeting = "Hi!"
print(greeting)
print("What is your name?")
name <- input
print("Dear ".+(name).+(", welcome!"))

2_DESUGARED
flatmap(greeting   => flatmap(_ => flatmap(_ => flatmap(name  => print("Dear ".+(name).+(", welcome!")))( input))(print("What is your name?")))(print(greeting)))( pure( "Hi!"))

3_TOKENS
[TokenIdf("flatmap"), TokenParenOpen(), TokenIdf("greeting"), TokenEqGr(), TokenIdf("flatmap"), TokenParenOpen(), TokenIdf("_"), TokenEqGr(), TokenIdf("flatmap"), TokenParenOpen(), TokenIdf("_"), TokenEqGr(), TokenIdf("flatmap"), TokenParenOpen(), TokenIdf("name"), TokenEqGr(), TokenIdf("print"), TokenParenOpen(), TokenLitStr("Dear "), TokenDot(), TokenIdf("+"), TokenParenOpen(), TokenIdf("name"), TokenParenClose(), TokenDot(), TokenIdf("+"), TokenParenOpen(), TokenLitStr(", welcome!"), TokenParenClose(), TokenParenClose(), TokenParenClose(), TokenParenOpen(), TokenIdf("input"), TokenParenClose(), TokenParenClose(), TokenParenOpen(), TokenIdf("print"), TokenParenOpen(), TokenLitStr("What is your name?"), TokenParenClose(), TokenParenClose(), TokenParenClose(), TokenParenOpen(), TokenIdf("print"), TokenParenOpen(), TokenIdf("greeting"), TokenParenClose(), TokenParenClose(), TokenParenClose(), TokenParenOpen(), TokenIdf("pure"), TokenParenOpen(), TokenLitStr("Hi!"), TokenParenClose(), TokenParenClose()]

4_EXPR
Expr_Call_1(
    Expr_Call_1(
        Expr_Idf("flatmap"),
        Expr_Lambda_1(
            Expr_Idf("greeting"),
            Expr_Call_1(
                Expr_Call_1(
                    Expr_Idf("flatmap"),
                    Expr_Lambda_1(
                        Expr_Idf("_"),
                        Expr_Call_1(
                            Expr_Call_1(
                                Expr_Idf("flatmap"),
                                Expr_Lambda_1(
                                    Expr_Idf("_"),
                                    Expr_Call_1(
                                        Expr_Call_1(
                                            Expr_Idf("flatmap"),
                                            Expr_Lambda_1(
                                                Expr_Idf("name"),
                                                Expr_Call_1(
                                                    Expr_Idf("print"),
                                                    Expr_Call_1(
                                                        Expr_Call_1(
                                                            Expr_Idf("+"),
                                                            Expr_Lit_Str(", welcome!")
                                                        ),
                                                        Expr_Call_1(
                                                            Expr_Call_1(
                                                                Expr_Idf("+"),
                                                                Expr_Idf("name")
                                                            ),
                                                            Expr_Lit_Str("Dear ")
                                                        )
                                                    )
                                                )
                                            )
                                        ),
                                        Expr_Idf("input")
                                    )
                                )
                            ),
                            Expr_Call_1(
                                Expr_Idf("print"),
                                Expr_Lit_Str("What is your name?")
                            )
                        )
                    )
                ),
                Expr_Call_1(
                    Expr_Idf("print"),
                    Expr_Idf("greeting")
                )
            )
        )
    ),
    Expr_Call_1(
        Expr_Idf("pure"),
        Expr_Lit_Str("Hi!")
    )
)

5_TYPED
Typed_Call_1(
    Typed_Call_1(
        Typed_Idf("flatmap", (Str => RIO[Unit]) => RIO[Str] => RIO[Unit]),
        Typed_Lambda_1(
            Typed_Idf("greeting", Str),
            Typed_Call_1(
                Typed_Call_1(
                    Typed_Idf("flatmap", (A => RIO[Unit]) => RIO[A] => RIO[Unit]),
                    Typed_Lambda_1(
                        Typed_Idf("_", A),
                        Typed_Call_1(
                            Typed_Call_1(
                                Typed_Idf("flatmap", (A => RIO[Unit]) => RIO[A] => RIO[Unit]),
                                Typed_Lambda_1(
                                    Typed_Idf("_", A),
                                    Typed_Call_1(
                                        Typed_Call_1(
                                            Typed_Idf("flatmap", (Str => RIO[Unit]) => RIO[Str] => RIO[Unit]),
                                            Typed_Lambda_1(
                                                Typed_Idf("name", Str),
                                                Typed_Call_1(
                                                    Typed_Idf("print", Str => RIO[Unit]),
                                                    Typed_Call_1(
                                                        Typed_Call_1(
                                                            Typed_Idf("+", Str => Str => Str),
                                                            Typed_Lit(", welcome!", Str),
                                                            Str => Str
                                                        ),
                                                        Typed_Call_1(
                                                            Typed_Call_1(
                                                                Typed_Idf("+", Str => Str => Str),
                                                                Typed_Idf("name", Str),
                                                                Str => Str
                                                            ),
                                                            Typed_Lit("Dear ", Str),
                                                            Str
                                                        ),
                                                        Str
                                                    ),
                                                    RIO[Unit]
                                                ),
                                                Str => RIO[Unit]
                                            ),
                                            RIO[Str] => RIO[Unit]
                                        ),
                                        Typed_Idf("input", RIO[Str]),
                                        RIO[Unit]
                                    ),
                                    A => RIO[Unit]
                                ),
                                RIO[Unit] => RIO[Unit]
                            ),
                            Typed_Call_1(
                                Typed_Idf("print", Str => RIO[Unit]),
                                Typed_Lit("What is your name?", Str),
                                RIO[Unit]
                            ),
                            RIO[Unit]
                        ),
                        A => RIO[Unit]
                    ),
                    RIO[Unit] => RIO[Unit]
                ),
                Typed_Call_1(
                    Typed_Idf("print", Str => RIO[Unit]),
                    Typed_Idf("greeting", Str),
                    RIO[Unit]
                ),
                RIO[Unit]
            ),
            Str => RIO[Unit]
        ),
        RIO[Str] => RIO[Unit]
    ),
    Typed_Call_1(
        Typed_Idf("pure", Str => RIO[Str]),
        Typed_Lit("Hi!", Str),
        RIO[Str]
    ),
    RIO[Unit]
)

6_SHOWN
(((lambda a_fb: lambda fa: BrickFlatmap(a_fb, fa)))((lambda greeting: (((lambda a_fb: lambda fa: BrickFlatmap(a_fb, fa)))((lambda _: (((lambda a_fb: lambda fa: BrickFlatmap(a_fb, fa)))((lambda _: (((lambda a_fb: lambda fa: BrickFlatmap(a_fb, fa)))((lambda name: ((lambda s: BrickPrint(s)))((((lambda right: lambda left: left + right))(", welcome!"))((((lambda right: lambda left: left + right))(name))("Dear "))))))(BrickInput()))))(((lambda s: BrickPrint(s)))("What is your name?")))))(((lambda s: BrickPrint(s)))(greeting)))))(((lambda a: BrickPure(a)))("Hi!"))

7_RUNNING
Hi!
What is your name?
Robert
Dear Robert, welcome!

>
