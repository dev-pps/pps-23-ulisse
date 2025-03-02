package ulisse.utils

import cats.Id
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.TestUtility.in
import ulisse.utils.Times.*
import ulisse.utils.Times.FluentDeclaration.h

import scala.language.postfixOps

class TimesTest extends AnyWordSpec with Matchers:
  "Time" should:
    "be converted in seconds" in:
      Time(1, 1, 1).toSeconds shouldBe 1 * Time.minutesInHour * Time.secondsInMinute + 1 * Time.secondsInMinute + 1
      Time(0, 0, 0).toSeconds shouldBe 0
      Time(0, 0, 59).toSeconds shouldBe 59
      Time(0, 59, 0).toSeconds shouldBe 59 * Time.secondsInMinute
      Time(
        23,
        59,
        59
      ).toSeconds shouldBe 23 * Time.minutesInHour * Time.secondsInMinute + 59 * Time.secondsInMinute + 59

  "ClockTime" should:
    "validate non in range values of hours and minutes" in:
      ClockTime(h = -12, m = 30) should be(Left(InvalidHours(Time(-12, 30, 0))))
      ClockTime(h = 30, m = 30) should be(Left(InvalidHours(Time(30, 30, 0))))
      ClockTime(h = 10, m = -30) should be(Left(InvalidMinutes(Time(10, -30, 0))))
      ClockTime(h = 10, m = 60) should be(Left(InvalidMinutes(Time(10, 60, 0))))

    "be added to another ClockTimes" in:
      h(10).m(45) + h(2).m(30) should be(h(13).m(15))
      h(10).m(59) + h(0).m(1) should be(h(11).m(0))
      h(0).m(59) + h(0).m(59) should be(h(1).m(58))
      h(23).m(59) + h(0).m(1) should be(h(0).m(0))

    "be subtracted to another ClockTimes" in:
      Id(Time(0, 0, 0)) - Id(Time(0, 10, 5)) shouldBe Id(Time(0, -10, -5))
      h(10).m(45) - h(1).m(25) shouldBe h(9).m(20)
      h(10).m(0) - h(1).m(10) shouldBe h(8).m(50)
      h(10).m(0) - h(11).m(0) shouldBe h(-1).m(0)

    "comparable to other ClockTime" in:
      h(10).m(45) greaterEqThan h(10).m(45) should be(true)
      h(10).m(45) greaterEqThan h(2).m(30) should be(true)
      h(2).m(30) greaterEqThan h(10).m(45) should be(false)
      h(2).m(30) sameAs h(10).m(45) should be(false)
      h(2).m(30) greaterThan h(10).m(45) should be(false)

    "be created with default value in case of invalid ClockTime" in:
      val invalidHour    = 25
      val invalidMinutes = 60
      val defaultTime    = Time(0, 0, 0)
      val clockTime      = ClockTime.withDefault(invalidHour, invalidMinutes)
      clockTime.asTime shouldBe Time(0, 0, 0)

    "return user clock time when hours and minutes values are valid using withDefault method" in:
      val validHours   = 12
      val validMinutes = 35
      val clockTime    = ClockTime.withDefault(validHours, validMinutes)
      clockTime.asTime shouldBe Time(validHours, validMinutes, 0)

    "returns a default time that depends on context strategy" in:
      import ulisse.utils.Times.ClockTime.DefaultTimeStrategy
      val invalidClockTime      = ClockTime(50, 70)
      given DefaultTimeStrategy = t => Time(t.h % 24, t.m % 60, 0)
      val defaultClockTime      = invalidClockTime.getOrDefault
      defaultClockTime.asTime shouldBe Time(2, 10, 0)
