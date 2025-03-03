package ulisse.utils

import cats.syntax.all.*
import cats.{Functor, Id, Monad}
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
    override def toString: String = s"$h:$m:$s"

  object Time:
    def apply(h: Hour, m: Minute, s: Second): Time = TimeImpl(h, m, s)
    def secondsToOverflowTime(s: Second): Time     = Id(Time(0, 0, s)) overflowSum Time(0, 0, 0)

    val secondsInMinute, minutesInHour = 60
    val hoursInDay                     = 24
    extension (time: Time)
      def toSeconds: Int = time.h * secondsInMinute * minutesInHour + time.m * secondsInMinute + time.s
      def toMinutes: Int = time.h * minutesInHour + time.m + time.s / secondsInMinute

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

  private def extractAndPerform[M[_]: Monad, T <: Time, R](
      t1: M[T],
      t2: M[T]
  )(f: (T, T) => M[R]): M[R] =
    for
      time1 <- t1
      time2 <- t2
      res   <- f(time1, time2)
    yield res

  extension (time: Either[ClockTimeErrors, ClockTime])
    def greaterEqThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ >= 0)

    def greaterThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ > 0)

    def sameAs(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ == 0)

  trait TimeConstructor[T]:
    def construct(h: Int, m: Int, s: Int): T

  given TimeConstructor[Time] with
    def construct(h: Int, m: Int, s: Int): Time = Time(h, m, s)

  given TimeConstructor[ClockTime] with
    def construct(h: Int, m: Int, s: Int): ClockTime = ClockTime(h, m).getOrDefault

  given TimeConstructor[Option[ClockTime]] with
    def construct(h: Int, m: Int, s: Int): Option[ClockTime] = ClockTime(h, m).toOption

  given TimeConstructor[Either[ClockTimeErrors, ClockTime]] with
    def construct(h: Int, m: Int, s: Int): Either[ClockTimeErrors, ClockTime] = ClockTime(h, m)

  given [M[_]: Functor]: Conversion[M[Time], M[ClockTime]] = _.map(t => ClockTime(t.h, t.m).getOrDefault)
  extension [M[_]: Monad, T <: Time](time: M[T])
    @targetName("add")
    def +(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateSum(t, t2)

    def overflowSum(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateOverflowSum(t, t2)

    def underflowSub(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateUnderflowSub(t, t2)

  /** Returns true if predicate on the two provided `ClockTime` is satisfied */
  private def checkCondition(
      t: Either[ClockTimeErrors, ClockTime],
      t2: Either[ClockTimeErrors, ClockTime]
  )(predicate: Int => Boolean): Boolean =
    val res = extractAndPerform(t, t2): (t, t2) =>
      Right(predicate(summon[Ordering[ClockTime]].compare(t, t2)))
    res.getOrElse(false)

  extension (time: ClockTime)
    @targetName("add")
    def ++(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      extractAndPerform(Right(time), time2): (t, t2) =>
        calculateSum(t, t2)

    @targetName("greaterEquals")
    def >=(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) >= 0

    @targetName("greater")
    def >(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) > 0

    @targetName("equals")
    def ===(time2: ClockTime): Boolean =
      summon[Ordering[ClockTime]].compare(time, time2) == 0

  private trait TimeBuildStrategy:
    def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int)

  private given defaultStrategy: TimeBuildStrategy with
    def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int) =
      (
        ((h % Time.hoursInDay) + Time.hoursInDay)           % Time.hoursInDay,
        ((m % Time.minutesInHour) + Time.minutesInHour)     % Time.minutesInHour,
        ((s % Time.secondsInMinute) + Time.secondsInMinute) % Time.secondsInMinute
      )

  private given overflowStrategy: TimeBuildStrategy with
    def buildTimeValue(h: Int, m: Int, s: Int): (Int, Int, Int) = (h, m % Time.minutesInHour, s % Time.secondsInMinute)

  private def calculateSum[M[_]: Monad, T <: Time](time1: T, time2: T)(
      using constructor: TimeConstructor[M[T]]
  ): M[T] =
    given TimeBuildStrategy = defaultStrategy
    buildTimeFromSeconds(time1.toSeconds + time2.toSeconds)

  private def calculateOverflowSum[M[_]: Monad, T <: Time](time1: T, time2: T)(using
      constructor: TimeConstructor[M[T]]
  ): M[T] =
    given TimeBuildStrategy = overflowStrategy
    buildTimeFromSeconds(time1.toSeconds + time2.toSeconds)

  private def calculateUnderflowSub[M[_]: Monad, T <: Time](time1: T, time2: T)(using
      constructor: TimeConstructor[M[T]]
  ): M[T] =
    given TimeBuildStrategy = overflowStrategy
    buildTimeFromSeconds(time1.toSeconds - time2.toSeconds)

  private def buildTimeFromSeconds[M[_]: Monad, T <: Time](seconds: Int)(using
      constructor: TimeConstructor[M[T]]
  )(using buildStrategy: TimeBuildStrategy): M[T] =
    constructor.construct.tupled(buildStrategy.buildTimeValue(
      seconds / (Time.secondsInMinute * Time.minutesInHour),
      (seconds / Time.secondsInMinute),
      seconds
    ))
