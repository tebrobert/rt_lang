package utils

object RtFail {
  def rtFail(msgs: String*) =
    throw new Exception (msgs.mkString)
}
