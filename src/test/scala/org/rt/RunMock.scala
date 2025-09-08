package org.rt

object RunMock {
  sealed trait RunMock

  case class InputMock(value: String) extends RunMock

  case class PrintMock(value: String) extends RunMock

  case class RunMocks(mockedCalls: List[RunMock])
}
