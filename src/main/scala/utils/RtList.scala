package utils

import utils.RtFail.rtFail

object RtList {
    def match_list[A, B](
        case_empty: Option[() => B] = None,
        case_at_least_1: Option[(A, List[A]) => B] = None,
        case_at_least_2: Option[(A, A, List[A]) => B] = None,
        case_at_least_3: Option[(A, A, A, List[A]) => B] = None,
        otherwise: Option[() => B] = None,
    )(list: List[A]): B =
        (case_empty, case_at_least_1, case_at_least_2, case_at_least_3, otherwise) match {
            case (Some(empty), Some(at_least_1), Some(at_least_2), Some(at_least_3), _) =>
                list match
                    case head1::head2::head3::tail => at_least_3(head1, head2, head3, tail)
                    case head1::head2::tail => at_least_2(head1, head2, tail)
                    case head::tail => at_least_1(head, tail)
                    case Nil => empty()
            case (Some(empty), Some(at_least_1), _, _, _) =>
                list match
                    case Nil => empty()
                    case head::tail => at_least_1(head, tail)
            case (Some(empty), Some(at_least_1), _, _, _) =>
                list match
                    case Nil => empty()
                    case head::tail => at_least_1(head, tail)
            case _ => rtFail("ni")
        }

}
