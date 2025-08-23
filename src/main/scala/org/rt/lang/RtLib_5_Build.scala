package org.rt.lang

object RtLib_5_Build {
  object Public {
    sealed trait Brick

    final case object BrickInput extends Brick

    final case class BrickPrint(s: String) extends Brick

    final case class BrickPure[ANY](a: ANY) extends Brick

    final case class BrickFlatmap[ANY](
      a_fb: ANY => Brick,
      fa: Brick,
    ) extends Brick
  }
}
