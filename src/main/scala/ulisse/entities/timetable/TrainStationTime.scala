package ulisse.entities.timetable

import ulisse.utils.Times.{ClockTime, Time}
import ulisse.utils.Times.FluentDeclaration.h

private type WaitTime = Int

/** Represents time information about arriving, departure and wait time of train into a station. */
trait TrainStationTime:
  def arriving: Option[ClockTime]
  def waitTime: Option[WaitTime]
  def departure: Option[ClockTime]

object TrainStationTime:
  /** Creates and returns [[TrainStationTime]] given `arriving`, `waitTime` and `departure` */
  def apply(arriving: Option[ClockTime], waitTime: Option[WaitTime], departure: Option[ClockTime]): TrainStationTime =
    TrainStationTimeImpl(arriving, waitTime, departure)

  def unapply(arg: TrainStationTime): Option[(Option[ClockTime], Option[WaitTime], Option[ClockTime])] =
    Some((arg.arriving, arg.waitTime, arg.departure))

  private case class TrainStationTimeImpl(
      arriving: Option[ClockTime],
      waitTime: Option[WaitTime],
      departure: Option[ClockTime]
  ) extends TrainStationTime

  /** Returns [[TrainStationTime]] with just `departure` time set. */
  def DepartureStationTime(departure: Option[ClockTime]): TrainStationTime =
    TrainStationTime(arriving = None, waitTime = None, departure = departure)

  /** Returns [[TrainStationTime]] with just given `arriving` time set. */
  def ArrivingStationTime(arriving: Option[ClockTime]): TrainStationTime =
    TrainStationTime(arriving = arriving, waitTime = None, departure = None)

  /** Returns [[TrainStationTime]] with given `arriving` and `waitTime` and calculated departure time. */
  def AutoStationTime(arriving: Option[ClockTime], waitTime: Option[WaitTime]): TrainStationTime =
    import ulisse.utils.Times.FluentDeclaration
    val departure =
      for
        a <- arriving
        u <- (a ++ Time(0, waitTime.getOrElse(0), 0).toClockTime).toOption
      yield u
    TrainStationTime(arriving = arriving, waitTime = waitTime, departure = departure)
