package ulisse.utils

import cats.Monad
import cats.Id
import cats.syntax.all.*
import ulisse.utils.Errors.{BaseError, ErrorMessage}

import scala.annotation.targetName

object Times:
  private type Hour   = Int
  private type Minute = Int
  private type Second = Int

  /** Errors that can be returned on ClockTime creation. */
  sealed trait ClockTimeErrors(val time: Time) extends BaseError
  final case class InvalidHours(t: Time)       extends ClockTimeErrors(t) with ErrorMessage("hours not in range [0,24]")
  final case class InvalidMinutes(t: Time)     extends ClockTimeErrors(t)
      with ErrorMessage("minutes not in range [0,59]")

  trait Time:
    def h: Hour
    def m: Minute
    def s: Second
    override def toString: String = s"$h:$m"

  object Time:
    def apply(h: Hour, m: Minute, s: Second): Time = TimeImpl(h, m, s)
    private case class TimeImpl(h: Hour, m: Minute, s: Second) extends Time

  trait ClockTime extends Time:
    def asTime: Time

  object ClockTime:
    private val maxDayHours        = 23
    private val minDayHours        = 0
    private val maxDayMinutes      = 59
    private val minDayMinutes      = 0
    private val ignoredSecondValue = 0

    def apply(h: Hour, m: Minute): Either[ClockTimeErrors, ClockTime] =
      val time = Time(h, m, ignoredSecondValue)
      for
        h <- ValidationUtils.validateRange(h, minDayHours, maxDayHours, InvalidHours(time))
        m <- ValidationUtils.validateRange(m, minDayMinutes, maxDayMinutes, InvalidMinutes(time))
      yield ClockTimeImpl(h, m)

    def unapply(ct: ClockTime): (Hour, Minute) = (ct.h, ct.m)

    def withDefault(h: Hour, m: Minute): ClockTime =
      ClockTime(h, m).getOrDefault

    trait DefaultTimeStrategy:
      def defaultTime(currentTime: Time): Time

    private object FixedTimeDefault extends DefaultTimeStrategy:
      override def defaultTime(currentTime: Time): Time = Time(0, 0, ignoredSecondValue)

    given predefinedDefaultTime: DefaultTimeStrategy = FixedTimeDefault

    extension (time: Either[ClockTimeErrors, ClockTime])
      def getOrDefault(using dts: DefaultTimeStrategy): ClockTime =
        import ulisse.utils.Times.ClockTimeErrors
        time match
          case Left(e) =>
            val dTime = dts.defaultTime(e.time)
            ClockTimeImpl(dTime.h, dTime.m)
          case Right(ct) => ct

    private case class ClockTimeImpl(h: Hour, m: Minute) extends ClockTime:
      override def asTime: Time = Time(h, m, ignoredSecondValue)
      override def s: Second    = ignoredSecondValue

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

  /** Ordering implementation for ClockTime.
    * ClockTimes are compared by hours (`h`) and then by minutes (`m`) if hours are equals.
    */
  given Ordering[ClockTime] = Ordering.by(ct => (ct.h, ct.m))

  private def extractAndPerform[R](
      t: Either[ClockTimeErrors, ClockTime],
      t2: Either[ClockTimeErrors, ClockTime]
  )(f: (ClockTime, ClockTime) => Either[ClockTimeErrors, R]): Either[ClockTimeErrors, R] =
    for
      ClockTime <- t
      time2     <- t2
      res       <- f(ClockTime, time2)
    yield res

  extension (time: Either[ClockTimeErrors, ClockTime])
    @targetName("add")
    def +(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateSumWithEither(t, t2)

    def greaterEqThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ >= 0)

    def greaterThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ > 0)

    def sameAs(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ == 0)

  extension (time: Option[ClockTime])
    @targetName("sub")
    def -(time2: Option[ClockTime]): Option[ClockTime] =
      for
        t   <- time
        t2  <- time2
        sub <- calculateSub(t, t2)
      yield sub

  given ((Int, Int, Int) => Either[ClockTimeErrors, ClockTime]) = (x, y, z) => ClockTime(x, y)
  given ((Int, Int, Int) => ClockTime)                          = (x, y, z) => ClockTime(x, y).getOrDefault
  given ((Int, Int, Int) => Option[ClockTime])                  = (x, y, z) => ClockTime(x, y).toOption
  given ((Int, Int, Int) => Time)                               = Time.apply
  extension [M[_]: Monad, T <: Time](time: M[T])
    @targetName("add")
    def +(time2: M[T])(using constructor: (Int, Int, Int) => M[T]): M[T] =
      for
        t  <- time
        t2 <- time2
        ct <- calculateSum(t, t2)
      yield ct

  /** Returns true if predicate on the two provided `ClockTime` is satisfied */
  private def checkCondition(
      t: Either[ClockTimeErrors, ClockTime],
      t2: Either[ClockTimeErrors, ClockTime]
  )(predicate: Int => Boolean): Boolean =
    val res = extractAndPerform[Boolean](t, t2): (t, t2) =>
      Right(predicate(summon[Ordering[ClockTime]].compare(t, t2)))
    res.getOrElse(false)

  extension (time: ClockTime)
    @targetName("add")
    def ++(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      for
        t2  <- time2
        sum <- calculateSumWithEither(time, t2)
      yield sum

    @targetName("greaterEquals")
    def >=(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) >= 0

    @targetName("greater")
    def >(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) > 0

    @targetName("equals")
    def ===(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) == 0

  private def calculateSumWithEither(t: ClockTime, t2: ClockTime): Either[ClockTimeErrors, ClockTime] =
    val minutesInHour = 60
    val hoursInDay    = 24
    val totalMinutes  = t.m + t2.m
    val extraHours    = totalMinutes / minutesInHour
    val minutes       = totalMinutes              % minutesInHour
    val hours         = (t.h + t2.h + extraHours) % hoursInDay
    ClockTime(hours, minutes)

  private def calculateSum[M[_]: Monad, T <: Time](time1: T, time2: T)(using
      constructor: (Int, Int, Int) => M[T]
  ): M[T] =
    val secondInMinutes, minutesInHour = 60
    val hoursInDay                     = 24
    val ts                             = time1.s + time2.s
    val tm                             = time1.m + time2.m
    val th                             = time1.h + time2.h
    val seconds                        = ts                  % secondInMinutes
    val extraMinutes                   = ts / secondInMinutes
    val minutes                        = (tm + extraMinutes) % minutesInHour
    val extraHours                     = (tm + extraMinutes) / minutesInHour
    val hours                          = (th + extraHours)   % hoursInDay
    constructor(hours, minutes, seconds)

  private def calculateSub(t: ClockTime, t2: ClockTime): Option[ClockTime] =
    val minutesInHour = 60
    val hoursInDay    = 24
    ((t.h, t2.h), (t.m, t2.m)) match
      case ((h1, h2), (m1, m2)) if m1 < m2 && h1 < 1 => None
      case ((h1, h2), (m1, m2)) if m1 < m2           => ClockTime(h1 - 1 - h2, m1 + minutesInHour - m2).toOption
      case ((h1, h2), (m1, m2))                      => ClockTime(h1 - h2, m1 - m2).toOption
