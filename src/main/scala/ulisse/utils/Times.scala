package ulisse.utils

import ulisse.utils.Errors.{BaseError, ErrorMessage}

import scala.annotation.targetName

object Times:

  /** Errors that can be returned on ClockTime creation.
    */
  sealed trait ClockTimeErrors      extends BaseError
  final case class InvalidHours()   extends ClockTimeErrors with ErrorMessage("hours not in range [0,24]")
  final case class InvalidMinutes() extends ClockTimeErrors with ErrorMessage("minutes not in range [0,59]")

  /** Represent clock time:
    *   - hours (`h`) must be between 0 and 23
    *   - minutes (`m`) must be between 0 and 59
    */
  trait ClockTime:
    def h: Int
    def m: Int
    override def toString: String = s"$h:$m"

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

  /** Ordering implementation for ClockTime.
    *
    * ClockTimes are compared by hours (`h`) and then by minutes (`m`) if hours are equals.
    */
  given Ordering[ClockTime] with
    override def compare(x: ClockTime, y: ClockTime): Int =
      val hoursComparison = x.h.compare(y.h)
      hoursComparison match
        case _ if hoursComparison == 0 => x.m.compare(y.m)
        case _                         => hoursComparison

  /** @param t
    *   first TimeClock
    * @param t2
    *   second TimeClock
    * @param predicate
    *   Predicate to convert compare result into boolean
    * @return
    *   True if predicate is satisfied
    */
  private def checkCondition(
      t: Either[ClockTimeErrors, ClockTime],
      t2: Either[ClockTimeErrors, ClockTime]
  )(predicate: Int => Boolean): Boolean =
    val res = extractAndPerform[Boolean](t, t2): (t, t2) =>
      Right(predicate(summon[Ordering[ClockTime]].compare(t, t2)))
    res.getOrElse(false)

  private def extractAndPerform[R](
      t: Either[ClockTimeErrors, ClockTime],
      t2: Either[ClockTimeErrors, ClockTime]
  )(f: (ClockTime, ClockTime) => Either[ClockTimeErrors, R]): Either[ClockTimeErrors, R] =
    for
      time  <- t
      time2 <- t2
      res   <- f(time, time2)
    yield res

  extension (time: Either[ClockTimeErrors, ClockTime])
    @targetName("add")
    def +(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      extractAndPerform(time, time2): (t, t2) =>
        calculateSum(t, t2)

    def greaterEqThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ >= 0)

    def greaterThan(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ > 0)

    def sameAs(time2: Either[ClockTimeErrors, ClockTime]): Boolean =
      checkCondition(time, time2)(_ == 0)

  extension (time: ClockTime)
    @targetName("add")
    def ++(time2: Either[ClockTimeErrors, ClockTime]): Either[ClockTimeErrors, ClockTime] =
      for
        t2  <- time2
        sum <- calculateSum(time, t2)
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

  private def calculateSum(t: ClockTime, t2: ClockTime): Either[ClockTimeErrors, ClockTime] =
    val totalMinutes = t.m + t2.m
    val extraHours   = totalMinutes / 60
    val minutes      = totalMinutes              % 60
    val hours        = (t.h + t2.h + extraHours) % 24
    ClockTime(hours, minutes)
