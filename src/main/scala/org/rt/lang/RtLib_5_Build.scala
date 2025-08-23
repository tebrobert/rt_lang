package org.rt.lang

object RtLib_5_Build {
  object Public {
    sealed trait Brick[A]

    final case object BrickInput extends Brick[String]

    final case class BrickPrint(s: String) extends Brick[Unit]

    final case class BrickPure[A](a: A) extends Brick[A]

    final case class BrickFlatmap[A, B](
      a_fb: A => Brick[B],
      fa: Brick[A],
    ) extends Brick[B]
  }
}
