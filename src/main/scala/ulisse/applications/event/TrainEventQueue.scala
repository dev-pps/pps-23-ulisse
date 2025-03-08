package ulisse.applications.event
import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.entities.train.Trains.TrainTechnology

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the train. */
trait TrainEventQueue:

  /** Add an event to create a train. */
  def addReadTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => Unit): Unit

  /** Add an event to create a train. */
  def addCreateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): Unit

  /** Add an event to update a train. */
  def addUpdateTrainEvent(update: (
      TrainManager,
      TechnologyManager[TrainTechnology],
      TimetableManager
  ) => (TrainManager, TimetableManager)): Unit

  /** Add an event to delete a train. */
  def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit

/** Companion object for the [[TrainEventQueue]] trait. */
object TrainEventQueue:

  /** Create a new queue state. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): TrainEventQueue = new TrainEventQueueImpl(events)

  private case class TrainEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends TrainEventQueue:

    override def addReadTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => Unit): Unit =
      events.offer(_ readTrain update)

    override def addCreateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): Unit =
      events.offer(_ createTrain update)

    override def addUpdateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology], TimetableManager) => (
        TrainManager,
        TimetableManager
    )): Unit =
      events.offer(_ updateTrain update)

    override def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager))
        : Unit = events.offer(_ updateTrainSchedule update)
