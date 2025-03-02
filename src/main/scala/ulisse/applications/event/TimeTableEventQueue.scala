package ulisse.applications.event
import ulisse.applications.AppState
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the timetable. */
trait TimeTableEventQueue:

  /** Add an event to read a timetable. */
  def addReadTimetableEvent(update: TimetableManager => Unit): Unit

  /** Add an event to create a timetable. */
  def addCreateTimetableEvent(update: (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit

  /** Add an event to update a timetable. */
  def addUpdateTimetableEvent(update: (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit = addCreateTimetableEvent(update)

  /** Add an event to delete a timetable. */
  def addDeleteTimetableEvent(update: TimetableManager => TimetableManager): Unit

/** Companion object for the [[TimeTableEventQueue]] trait. */
object TimeTableEventQueue:

  /** Create a new queue state. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): TimeTableEventQueue = TimeTableEventQueueImpl(events)

  private case class TimeTableEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState])
      extends TimeTableEventQueue:

    override def addReadTimetableEvent(update: TimetableManager => Unit): Unit = events.offer(_ readTimetable update)

    override def addCreateTimetableEvent(update: (
        StationManager,
        RouteManager,
        TrainManager,
        TimetableManager
    ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit =
      events.offer(_ updateRailwaySchedule update)

    override def addDeleteTimetableEvent(update: TimetableManager => TimetableManager): Unit =
      events.offer(_ updateTimetable update)
