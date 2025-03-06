package ulisse.utils

import cats.Id
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Utils.TestUtility.in
import ulisse.utils.Times.*
import ulisse.utils.Times.FluentDeclaration.h

import scala.language.postfixOps

class TimesTest extends AnyWordSpec with Matchers:

  "Time" when:
    "Created" should:
      "don't adapt to time format" in:
        Time(0, 0, 100).s shouldBe 100
        Time(0, 100, 0).m shouldBe 100
        Time(100, 0, 0).h shouldBe 100

    "Created from seconds" should:
      "adapt to time format" in:
        Time.secondsToTime(3661) shouldBe Time(1, 1, 1)
        Time.secondsToTime(0) shouldBe Time(0, 0, 0)
        Time.secondsToTime(59) shouldBe Time(0, 0, 59)
        Time.secondsToTime(60) shouldBe Time(0, 1, 0)
        Time.secondsToTime(3600) shouldBe Time(1, 0, 0)
        Time.secondsToTime(100000) shouldBe Time(3, 46, 40)

      "adapt to time format with overflow" in:
        Time.secondsToOverflowTime(3661) shouldBe Time(1, 1, 1)
        Time.secondsToOverflowTime(0) shouldBe Time(0, 0, 0)
        Time.secondsToOverflowTime(59) shouldBe Time(0, 0, 59)
        Time.secondsToOverflowTime(60) shouldBe Time(0, 1, 0)
        Time.secondsToOverflowTime(3600) shouldBe Time(1, 0, 0)
        Time.secondsToOverflowTime(100000) shouldBe Time(27, 46, 40)

  "Time" should:
    "be converted in seconds" in:
      Time(1, 1, 1).toSeconds shouldBe 1 * Time.minutesInHour * Time.secondsInMinute + 1 * Time.secondsInMinute + 1
      Time(0, 0, 0).toSeconds shouldBe 0
      Time(0, 0, 59).toSeconds shouldBe 59
      Time(0, 59, 0).toSeconds shouldBe 59 * Time.secondsInMinute

    "be converted in minutes" in:
      Time(1, 1, 1).toMinutes shouldBe 1 * Time.minutesInHour + 1
      Time(0, 0, 0).toMinutes shouldBe 0
      Time(0, 0, 59).toMinutes shouldBe 0
      Time(0, 59, 80).toMinutes shouldBe 60
      Time(23, 59, 59).toMinutes shouldBe 23 * Time.minutesInHour + 59

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
      h(23).m(59) + h(23).m(59) should be(h(23).m(58))

    "be added with overflow to another ClockTimes" in:
      Id(Time(23, 59, 0)) overflowSum Id(Time(0, 10, 5)) shouldBe Id(Time(24, 9, 5))
      Id(Time(27, 0, 0)) overflowSum Id(Time(3, 1, 0)) shouldBe Id(Time(30, 1, 0))
      h(10).m(45) overflowSum h(1).m(25) shouldBe h(12).m(10)

    "be subtracted with underflow to another ClockTimes" in:
      Id(Time(0, 0, 0)) underflowSub Id(Time(0, 10, 5)) shouldBe Id(Time(0, -10, -5))
      h(10).m(45) underflowSub h(1).m(25) shouldBe h(9).m(20)
      h(10).m(0) underflowSub h(1).m(10) shouldBe h(8).m(50)
      h(10).m(0) underflowSub h(11).m(0) shouldBe h(-1).m(0)

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

  "Milliseconds" should:
    "be converted in time" in:
      0.toTime shouldBe Time(0, 0, 0)
      1000.toTime shouldBe Time(0, 0, 1)
      60000.toTime shouldBe Time(0, 1, 0)
      3661000.toTime shouldBe Time(1, 1, 1)
