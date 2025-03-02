package ulisse.applications

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.entities.simulation.Simulations.SimulationData
import ulisse.entities.train.Trains.TrainTechnology

object EventQueues:
  /** Event queue to update the station. */
  trait StationEventQueue:
    /** Add an event to read a station. */
    def addReadStationEvent(event: StationManager => Unit): Unit

    /** Add an event to create a station. */
    def addCreateStationEvent(event: StationManager => StationManager): Unit

    /** Add an event to update a station. */
    def addUpdateStationEvent(event: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit

    /** Add an event to delete a station. */
    def addDeleteStationEvent(event: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = addUpdateStationEvent(event)

  /** Event queue to update the route. */
  trait RouteEventQueue:
    /** Add an event to read a route. */
    def addReadRouteEvent(event: RouteManager => Unit): Unit

    /** Add an event to create a route. */
    def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit

    /** Add an event to update a route. */
    def addUpdateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
      addCreateRouteEvent(update)

    /** Add an event to delete a route. */
    def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager)): Unit

  /** Event queue to update the train. */
  trait TrainEventQueue:
    /** Add an event to create a train. */
    def addReadTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => Unit): Unit

    /** Add an event to create a train. */
    def addCreateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): Unit

    /** Add an event to update a train. */
    def addUpdateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): Unit =
      addCreateTrainEvent(update)

    /** Add an event to delete a train. */
    def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit

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

  /** Event queue to update the simulation. */
  trait SimulationEventQueue:
    /** Add an event to read a simulation. */
    def addReadSimulationEvent(update: SimulationData => Unit): Unit

    /** Add an event to create a simulation. */
    def addCreateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit

    /** Add an event to update the simulation. */
    def addUpdateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      addCreateSimulationEvent(update)

    /** Add an event to delete the simulation. */
    def addDeleteSimulationEvent(update: SimulationManager => SimulationManager): Unit
