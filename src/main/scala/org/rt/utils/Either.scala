package org.rt.utils

object Either {
  extension [A, B](either: Either[A, B]) {
    inline def rtMatchEither[C](
        inline caseLeft: A => C,
        inline caseRight: B => C,
    ): C = {
      either match {
        case Left(value)  => caseLeft(value)
        case Right(value) => caseRight(value)
      }
    }
  }
}
