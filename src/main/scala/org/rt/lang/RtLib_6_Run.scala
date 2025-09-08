package org.rt.lang

import org.rt.lang.RtLib_5_Build.Public.{Built, BuiltRio}
import org.rt.utils.RtFail.rtFail
import zio.UIO

object RtLib_6_Run {
  def run(
    built: Built,
  ): UIO[Built] =
    built match {
      case BuiltRio(run) => run
      case _ => rtFail("runtime")
    }
}
