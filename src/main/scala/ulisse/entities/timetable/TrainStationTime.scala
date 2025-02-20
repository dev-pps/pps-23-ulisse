package ulisse.entities.timetable

import ulisse.utils.Times.ClockTime
import ulisse.utils.Times.FluentDeclaration.h

private type WaitTime = Int

trait TrainStationTime:
  def arriving: Option[ClockTime]
  def waitTime: Option[WaitTime]
  def departure: Option[ClockTime]

object TrainStationTime:
  def apply(arriving: Option[ClockTime], waitTime: Option[WaitTime], departure: Option[ClockTime]): TrainStationTime =
    TrainStationTimeImpl(arriving, waitTime, departure)

  private case class TrainStationTimeImpl(
      arriving: Option[ClockTime],
      waitTime: Option[WaitTime],
      departure: Option[ClockTime]
  ) extends TrainStationTime

  def StartScheduleTime(departure: Option[ClockTime]): TrainStationTime =
    TrainStationTime(arriving = None, waitTime = None, departure = departure)

  def EndScheduleTime(arriving: Option[ClockTime]): TrainStationTime =
    TrainStationTime(arriving = arriving, waitTime = None, departure = None)

  def AutoScheduleTime(arriving: Option[ClockTime], waitTime: Option[WaitTime]): TrainStationTime =
    import ulisse.utils.Times.FluentDeclaration
    val departure =
      for
        a <- arriving
        u <- (a ++ h(0).m(waitTime.getOrElse(0))).toOption
      yield u
    TrainStationTime(arriving = arriving, waitTime = waitTime, departure = departure)
