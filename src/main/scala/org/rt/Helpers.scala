package org.rt

object Helpers {
  extension [ANY](value: ANY) {
    def tapDebug(handle: ANY => Unit): ANY = {
      handle(value)
      value
    }
  }
}
