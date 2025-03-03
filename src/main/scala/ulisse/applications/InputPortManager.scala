package ulisse.applications

import ulisse.applications.ports.{RoutePorts, SimulationPorts, StationPorts, TimetablePorts, TrainPorts}
import ulisse.applications.useCases.{RouteService, SimulationService, StationService, TimetableService, TrainService}

/** Represents the input port manager of the application. */
trait InputPortManager:
  def station: StationPorts.Input
  def route: RoutePorts.Input
  def train: TrainPorts.Input
  def timetable: TimetablePorts.Input

object InputPortManager:

  /** Creates a new instance of the input port manager. */
  def apply(eventQueue: EventQueue): InputPortManager = new InputPortManagerImpl(eventQueue)

  private case class InputPortManagerImpl(
      eventQueue: EventQueue,
      station: StationPorts.Input,
      route: RoutePorts.Input,
      train: TrainPorts.Input,
      timetable: TimetablePorts.Input
  ) extends InputPortManager:

    def this(eventQueue: EventQueue) =
      this(
        eventQueue,
        StationService(eventQueue),
        RouteService(eventQueue),
        TrainService(eventQueue),
        TimetableService(eventQueue)
      )
