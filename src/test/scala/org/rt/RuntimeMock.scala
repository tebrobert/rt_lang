package org.rt

object RuntimeMock {
  sealed trait RuntimeMock

  case class InputMock(value: String) extends RuntimeMock

  case class PrintMock(value: String) extends RuntimeMock
}
