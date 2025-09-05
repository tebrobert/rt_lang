package org.rt.lang

import org.rt.lang.RtLib_5_Build.Public.{Built, BuiltBrick}
import org.rt.utils.RtFail.rtFail

object RtLib_6_Run {
  def run(
    built: Built,
  ): Unit =
    built match {
      case BuiltBrick(run) => run()
      case _ => rtFail("runtime")
    }
}
