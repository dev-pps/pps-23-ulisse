package ulisse.utils

import cats.syntax.all.*
import cats.{Functor, Id, Monad}
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.{BaseError, ErrorMessage}
import ulisse.utils.OptionUtils.when
import ulisse.utils.Times.FluentDeclaration.h

import scala.annotation.targetName

/** Contains all entities and utility about [[Time]] and [[TimeClock]] */
object Times:
  private type Hour   = Int
  private type Minute = Int
  private type Second = Int
  type Milliseconds   = Long
  extension (millis: Milliseconds)
    /** Converts milliseconds to OverflowTime. */
    def toTime: Time = Time.secondsToOverflowTime((millis / 1000).toInt)

  /** Errors that can be returned on ClockTime creation. */
  sealed trait ClockTimeErrors(val time: Time) extends BaseError
  final case class InvalidHours(t: Time)       extends ClockTimeErrors(t) with ErrorMessage("hours not in range [0,24]")
  final case class InvalidMinutes(t: Time)     extends ClockTimeErrors(t)
      with ErrorMessage("minutes not in range [0,59]")

  /** Trait representing concept of time with hours, minutes and seconds. */
  trait Time:
    /** Hours value */
    def h: Hour

    /** Minutes value */
    def m: Minute

    /** Seconds value */
    def s: Second
    override def toString: String = s"$h:$m:$s"
    override def equals(that: Any): Boolean =
      that match
        case t: Time => t.h == h && t.m == m && t.s == s
        case _       => false

  /** Companion object of trait [[Time]] */
  object Time:
    /** Creates a `Time` instance. */
    def apply(h: Hour, m: Minute, s: Second): Time = TimeImpl(h, m, s)

    /** Unapply methods returning tuple with hours, minutes and seconds. */
    def unapply(t: Time): (Hour, Minute, Second) = (t.h, t.m, t.s)

    /** Creates a `Time` instance from seconds and go beyond 24h if is needed. */
    def secondsToOverflowTime(s: Second): Time = Id(Time(0, 0, s)) overflowSum Time(0, 0, 0)

    /** Creates a `Time` instance from seconds and reset times every time a day is ended. */
    def secondsToTime(s: Second): Time = Id(Time(0, 0, s)) + Time(0, 0, 0)

    val secondsInMinute, minutesInHour = 60
    val hoursInDay                     = 24
    extension (time: Time)
      /** Converts the time to seconds. */
      def toSeconds: Int = time.h * secondsInMinute * minutesInHour + time.m * secondsInMinute + time.s

      /** Converts the time to minutes. */
      def toMinutes: Int = time.h * minutesInHour + time.m + time.s / secondsInMinute

    private case class TimeImpl(h: Hour, m: Minute, s: Second) extends Time

  /** A particular type of [[Time]] of time with constrain that minutes and hours must be valid on creation.
    * Valid is mean that minutes must be between o to 59 and hours between 0 and 23.
    */
  trait ClockTime extends Time:
    /** Returns equivalent [[Time]] entity of ClockTime. Seconds value of returned Time is always zero. */
    def asTime: Time

  /** Companion object of trait [[ClockTime]] */
  object ClockTime:
    private val maxDayHours        = 23
    private val minDayHours        = 0
    private val maxDayMinutes      = 59
    private val minDayMinutes      = 0
    private val ignoredSecondValue = 0

    /** Creates ClockTime returning a Left in case of violation of ClockTime boundaries. */
    def apply(h: Hour, m: Minute): Either[ClockTimeErrors, ClockTime] =
      val time = Id(Time(0, 0, 0)) overflowSum Id(Time(h, m, ignoredSecondValue))
      for
        h <- ValidationUtils.validateRange(h, minDayHours, maxDayHours, InvalidHours(time))
        m <- ValidationUtils.validateRange(m, minDayMinutes, maxDayMinutes, InvalidMinutes(time))
      yield ClockTimeImpl(h, m)

    /** Unapply methods returning tuple with hours and minutes. */
    def unapply(ct: ClockTime): (Hour, Minute) = (ct.h, ct.m)

    /** Creates new ClockTime. In case of some creation error is returned a
      * default ClockTime using a [[DefaultTimeStrategy]]
      */
    def withDefault(h: Hour, m: Minute): ClockTime =
      ClockTime(h, m).getOrDefault

    /** Default time strategy */
    trait DefaultTimeStrategy:
      /** Given a `currentTime` returns a default one. */
      def defaultTime(currentTime: Time): Time

    /** Strategy that return always as default a Time with hours, minutes and seconds zero valued. */
    private object FixedTimeDefault extends DefaultTimeStrategy:
      override def defaultTime(currentTime: Time): Time = Time(0, 0, ignoredSecondValue)

    /** Default given instance of DefaultTimeStrategy */
    given predefinedDefaultTime: DefaultTimeStrategy = FixedTimeDefault

    extension (time: Either[ClockTimeErrors, ClockTime])
      /** Returns a default ClockTime using [[DefaultTimeStrategy]] for calculation of default ClockTime. */
      def getOrDefault(using dts: DefaultTimeStrategy): ClockTime =
        time match
          case Left(e) =>
            val dTime = dts.defaultTime(e.time)
            ClockTimeImpl(dTime.h, dTime.m)
          case Right(ct) => ct

    private case class ClockTimeImpl(h: Hour, m: Minute) extends ClockTime:
      override def asTime: Time = Time(h, m, ignoredSecondValue)
      override def s: Second    = ignoredSecondValue

  /** Contains classes and extension nmethods for creation of ClockTime in a more readable way. */
  object FluentDeclaration:
    /** ClockTime builder that contains `hours` */
    case class HoursBuilder(hours: Int)

    /** ClockTime builder with given hours `h` */
    infix def h(h: Int): HoursBuilder = HoursBuilder(h)

    extension (hb: HoursBuilder)
      /** Returns ClockTime with `minutes` and previous given hours */
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

  extension (time: Time)
    /** Returns `time` into ClockTime. It returns Left if given Time */
    def toClockTime: Either[ClockTimeErrors, ClockTime] =
      h(time.h).m(time.m)

  extension (time: Either[ClockTimeErrors, ClockTime])
    def greaterEqThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ >= 0)

    def greaterThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ > 0)

    def sameAs(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ == 0)

  /** Typeclass to provide different time constructors */
  sealed trait TimeConstructor[T]:
    def construct(h: Int, m: Int, s: Int): T

  /** Time constructor for plain time */
  given TimeConstructor[Time] with
    def construct(h: Int, m: Int, s: Int): Time = Time(h, m, s)

  /** Time constructor for Option[Time] */
  given optionTimeConstructor: TimeConstructor[Option[Time]] with
    def construct(h: Int, m: Int, s: Int): Option[Time] = Some(Time(h, m, s))

  /** Time constructor for ClockTime */
  given TimeConstructor[ClockTime] with
    def construct(h: Int, m: Int, s: Int): ClockTime = ClockTime(h, m).getOrDefault

  /** Time constructor for Option[ClockTime] */
  given optionClockTimeConstructor: TimeConstructor[Option[ClockTime]] with
    def construct(h: Int, m: Int, s: Int): Option[ClockTime] = ClockTime(h, m).toOption

  /** Time constructor for Either[ClockTimeErrors, ClockTime] */
  given TimeConstructor[Either[ClockTimeErrors, ClockTime]] with
    def construct(h: Int, m: Int, s: Int): Either[ClockTimeErrors, ClockTime] = ClockTime(h, m)

  /** Implicit conversion from time to ClockTime */
  given [M[_]: Functor]: Conversion[M[Time], M[ClockTime]] = _.map(t => ClockTime(t.h, t.m).getOrDefault)
  extension [M[_]: Monad, T <: Time](time: M[T])
    /** Adds two times */
    @targetName("add")
    def +(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateSum(t, t2)

    /** Adds two times with overflow */
    def overflowSum(time2: M[T])(using constructor: TimeConstructor[M[T]]): M[T] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateOverflowSum(t, t2)

    /** Subtracts two times with underflow */
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

  private def adaptTimeUnitToBound(timeUnit: Int, timeBound: Int): Int =
    ((timeUnit % timeBound) + timeBound) % timeBound

  private def calculateSum[M[_]: Monad, T <: Time](time1: T, time2: T)(
      using constructor: TimeConstructor[M[T]]
  ): M[T] =
    val secondsInADay = Time.secondsInMinute * Time.minutesInHour * Time.hoursInDay
    buildOverflowTimeFromSeconds(adaptTimeUnitToBound(time1.toSeconds + time2.toSeconds, secondsInADay))

  private def calculateOverflowSum[M[_]: Monad, T <: Time](time1: T, time2: T)(using
      constructor: TimeConstructor[M[T]]
  ): M[T] =
    buildOverflowTimeFromSeconds(time1.toSeconds + time2.toSeconds)

  private def calculateUnderflowSub[M[_]: Monad, T <: Time](time1: T, time2: T)(using
      constructor: TimeConstructor[M[T]]
  ): M[T] =
    buildOverflowTimeFromSeconds(time1.toSeconds - time2.toSeconds)

  private def buildOverflowTimeFromSeconds[M[_]: Monad, T <: Time](seconds: Int)(using
      constructor: TimeConstructor[M[T]]
  ): M[T] =
    constructor.construct.tupled(
      seconds / (Time.secondsInMinute * Time.minutesInHour),
      seconds / Time.secondsInMinute % Time.minutesInHour,
      seconds                        % Time.secondsInMinute
    )
