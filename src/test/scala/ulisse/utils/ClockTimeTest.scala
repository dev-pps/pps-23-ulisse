package ulisse.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.utils.Times.*
import ulisse.utils.Times.FluentDeclaration.{h, HoursBuilder}

import scala.language.postfixOps

class ClockTimeTest extends AnyWordSpec with Matchers:
  "ClockTime" should:
    "validate non in range values of hours and minutes" in:
      ClockTime(h = -12, m = 30) should be(Left(InvalidHours()))
      ClockTime(h = 30, m = 30) should be(Left(InvalidHours()))
      ClockTime(h = 10, m = -30) should be(Left(InvalidMinutes()))
      ClockTime(h = 10, m = 60) should be(Left(InvalidMinutes()))

    "be added to another ClockTimes" in:
      h(10).m(45) + h(2).m(30) should be(h(13).m(15))
      h(10).m(59) + h(0).m(1) should be(h(11).m(0))
      h(0).m(59) + h(0).m(59) should be(h(1).m(58))
      h(23).m(59) + h(0).m(1) should be(h(0).m(0))
//
//    "be subtracted to another ClockTimes" in :
//      ClockTime(10, 25) - ClockTime(1, 25) should be(ClockTime(11, 50))
//      ClockTime(10, 59) - ClockTime(0, 1) should be(ClockTime(11, 0))
//      ClockTime(24, 59) - ClockTime(0, 1) should be(ClockTime(1, 0))
//      ClockTime(24, 59) - ClockTime(0, 1) should be(ClockTime(0, 0))
