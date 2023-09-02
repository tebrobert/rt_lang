Launch a code sample:
python3 rt.py sample.rt

Launch a code sample and see how it really works:
python3 rt.py sample.rt --dev

Get help:
python3 rt.py -h



*********

EXAMPLE OUTPUT

> cat sample.rt
greeting = "Hey! What is your name?"
print(greeting)
name <- input
print("Welcome, ...")
print(name)
> python3 rt.py sample.rt 
Hey! What is your name?
Robert
Welcome, ...
Robert
>



*********

EXAMPLE OUTPUT

> cat sample.rt
greeting = "Hey! What is your name?"
print(greeting)
name <- input
print("Welcome, ...")
print(name)
> python3 rt.py sample.rt --dev
1_CODE:
greeting = "Hey! What is your name?"
print(greeting)
name <- input
print("Welcome, ...")
print(name)


2_DESUGARED:
flatmap(greeting   => flatmap(_ => flatmap(name  => flatmap(_ => print(name))(print("Welcome, ...")))( input))(print(greeting)))( pure( "Hey! What is your name?"))

3_TOKENS:
[Token_Idf("flatmap"), Token_Paren_Open(), Token_Idf("greeting"), Token_Eq_Gr(), Token_Idf("flatmap"), Token_Paren_Open(), Token_Idf("_"), Token_Eq_Gr(), Token_Idf("flatmap"), Token_Paren_Open(), Token_Idf("name"), Token_Eq_Gr(), Token_Idf("flatmap"), Token_Paren_Open(), Token_Idf("_"), Token_Eq_Gr(), Token_Idf("print"), Token_Paren_Open(), Token_Idf("name"), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Open(), Token_Idf("print"), Token_Paren_Open(), Token_Lit_Str("Welcome, ..."), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Open(), Token_Idf("input"), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Open(), Token_Idf("print"), Token_Paren_Open(), Token_Idf("greeting"), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Close(), Token_Paren_Open(), Token_Idf("pure"), Token_Paren_Open(), Token_Lit_Str("Hey! What is your name?"), Token_Paren_Close(), Token_Paren_Close()]

4_EXPR:
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
                                    Expr_Idf("name"),
                                    Expr_Call_1(
                                        Expr_Call_1(
                                            Expr_Idf("flatmap"),
                                            Expr_Lambda_1(
                                                Expr_Idf("_"),
                                                Expr_Call_1(
                                                    Expr_Idf("print"),
                                                    Expr_Idf("name")
                                                )
                                            )
                                        ),
                                        Expr_Call_1(
                                            Expr_Idf("print"),
                                            Expr_Lit_Str("Welcome, ...")
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
                    Expr_Idf("greeting")
                )
            )
        )
    ),
    Expr_Call_1(
        Expr_Idf("pure"),
        Expr_Lit_Str("Hey! What is your name?")
    )
)

5_TYPED:
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
                                Typed_Idf("flatmap", (Str => RIO[Unit]) => RIO[Str] => RIO[Unit]),
                                Typed_Lambda_1(
                                    Typed_Idf("name", Str),
                                    Typed_Call_1(
                                        Typed_Call_1(
                                            Typed_Idf("flatmap", (A => RIO[Unit]) => RIO[A] => RIO[Unit]),
                                            Typed_Lambda_1(
                                                Typed_Idf("_", A),
                                                Typed_Call_1(
                                                    Typed_Idf("print", Str => RIO[Unit]),
                                                    Typed_Idf("name", Str),
                                                    RIO[Unit]
                                                ),
                                                A => RIO[Unit]
                                            ),
                                            RIO[Unit] => RIO[Unit]
                                        ),
                                        Typed_Call_1(
                                            Typed_Idf("print", Str => RIO[Unit]),
                                            Typed_Lit("Welcome, ...", Str),
                                            RIO[Unit]
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
        Typed_Lit("Hey! What is your name?", Str),
        RIO[Str]
    ),
    RIO[Unit]
)

6_SHOWN:
(((lambda a_fb: lambda fa: Flatmap(a_fb, fa)))((lambda greeting: (((lambda a_fb: lambda fa: Flatmap(a_fb, fa)))((lambda _: (((lambda a_fb: lambda fa: Flatmap(a_fb, fa)))((lambda name: (((lambda a_fb: lambda fa: Flatmap(a_fb, fa)))((lambda _: ((lambda s: Print(s)))(name))))(((lambda s: Print(s)))("Welcome, ...")))))(Input()))))(((lambda s: Print(s)))(greeting)))))(((lambda a: Pure(a)))("Hey! What is your name?"))

7_RUNNING:
Hey! What is your name?
Robert
Welcome, ...
Robert
>
