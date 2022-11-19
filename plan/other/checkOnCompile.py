def f(x: {_ > 1}): {_ > 0} =
    1 / (x - 1)

def concat[A](xs: List[A], ys: List[A]
): List[A] {_.len == xs.len + ys.len} = ???

def div(x, y: {_ != 0}) = ???

def input: URIO[Console, String]
def toInt: String => Either[ValueError, Int]
def check[A]: (A, A => Bool) => Either[ValueError, A]
def retry[R, E, A]: RIO[R, E, A] => URIO[R, A]
def main: URIO[Args & Console, Unit] =
    x <- input() >>= toInt() >>= check(_ != 0) $ retry()
    print(1 / x)

