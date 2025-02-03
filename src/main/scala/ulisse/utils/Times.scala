package ulisse.utils

import ulisse.utils.Errors.ErrorMessage
import scala.annotation.targetName

object Times:

  sealed trait ClockTimeErrors
  final case class InvalidHours()   extends ClockTimeErrors with ErrorMessage("hours not in range [0,24]")
  final case class InvalidMinutes() extends ClockTimeErrors with ErrorMessage("minutes not in range [0,59]")

  trait ClockTime:
    def h: Int
    def m: Int

  object ClockTime:
    def apply(h: Int, m: Int): Either[ClockTimeErrors, ClockTime] =
      for
        h <- ValidationUtils.validateRange(h, 0, 23, InvalidHours())
        m <- ValidationUtils.validateRange(m, 0, 59, InvalidMinutes())
      yield ClockTimeImpl(h, m)

    private case class ClockTimeImpl(h: Int, m: Int) extends ClockTime

  object FluentDeclaration:
    /** ClockTime builder
      * @param hours
      *   hours
      */
    case class HoursBuilder(hours: Int)

    /** @param h
      *   hours
      * @return
      *   ClockTime builder
      */
    infix def h(h: Int): HoursBuilder = HoursBuilder(h)

    extension (hb: HoursBuilder)
      /** @param minutes
        *   minute
        * @return
        *   ClockTime with minutes and previous given hours
        */
      def m(minutes: Int): Either[ClockTimeErrors, ClockTime] = ClockTime(hb.hours, minutes)

  extension (time: Either[ClockTimeErrors, ClockTime])
    @targetName("add")
    def +(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      for
        t   <- time
        t2  <- time2
        sum <- calculateSum(t, t2)
      yield sum

  extension (time: ClockTime)
    @targetName("add")
    def ++(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      for
        t2  <- time2
        sum <- calculateSum(time, t2)
      yield sum

  private def calculateSum(t: ClockTime, t2: ClockTime): Either[ClockTimeErrors, ClockTime] =
    val totalMinutes = t.m + t2.m
    val extraHours   = totalMinutes / 60
    val minutes      = totalMinutes              % 60
    val hours        = (t.h + t2.h + extraHours) % 24
    ClockTime(hours, minutes)
