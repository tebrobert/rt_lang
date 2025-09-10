package org.rt

object Helpers {
  extension [ANY](value: ANY) {
    def tapPrint(handle: ANY => String): ANY = {
      //println(handle(value))
      value
    }
  }
}
