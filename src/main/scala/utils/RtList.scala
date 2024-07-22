package utils

import utils.RtFail.rtFail

object RtList {
    def match_list[A, B](
        case_empty: Option[() => B] = None,
        case_at_least_1: Option[(A, List[A]) => B] = None,
        //case_at_least_2 = None,
        //case_at_least_3 = None,
        otherwise: Option[() => B] = None,
    )(list: List[A]): B =
        (case_empty, case_at_least_1, otherwise) match {
            case (Some(empty), Some(at_least_1), _) =>
                list match
                    case Nil => empty()
                    case head::tail => at_least_1(head, tail)
            case _ => rtFail("ni")
        }

}
