package utils

object RtFail {
  def rtFail(msgs: String*) =
    throw new Exception (msgs.mkString)

  def fail_if(cond: Boolean, msg: String*) =
    if (cond)
        rtFail(msg:_*)

  def rt_assert(cond: Boolean, msg: String = "Assertion error.") =
    fail_if(!cond, msg)


}
