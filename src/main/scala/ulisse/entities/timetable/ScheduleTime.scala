package ulisse.entities.timetable

import ulisse.utils.Times.ClockTime
import ulisse.utils.Times.FluentDeclaration.h

private type WaitTime = Int

trait ScheduleTime:
  def arriving: Option[ClockTime]
  def waitTime: Option[WaitTime]
  def departure: Option[ClockTime]

object ScheduleTime:
  def apply(arriving: Option[ClockTime], waitTime: Option[WaitTime], departure: Option[ClockTime]): ScheduleTime =
    ScheduleTimeImpl(arriving, waitTime, departure)

  private case class ScheduleTimeImpl(
      arriving: Option[ClockTime],
      waitTime: Option[WaitTime],
      departure: Option[ClockTime]
  ) extends ScheduleTime

  def StartScheduleTime(departure: Option[ClockTime]): ScheduleTime =
    ScheduleTime(arriving = None, waitTime = None, departure = departure)

  def EndScheduleTime(arriving: Option[ClockTime]): ScheduleTime =
    ScheduleTime(arriving = arriving, waitTime = None, departure = None)

  def AutoScheduleTime(arriving: Option[ClockTime], waitTime: Option[WaitTime]): ScheduleTime =
    import ulisse.utils.Times.FluentDeclaration
    val departure =
      for
        a <- arriving
        u <- (a ++ h(0).m(waitTime.getOrElse(0))).toOption
      yield u
    ScheduleTime(arriving = arriving, waitTime = waitTime, departure = departure)
