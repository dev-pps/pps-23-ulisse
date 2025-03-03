package ulisse.applications

import ulisse.applications.ports.{RoutePorts, SimulationPorts, StationPorts, TimetablePorts, TrainPorts}
import ulisse.applications.useCases.{RouteService, SimulationService, StationService, TimetableService, TrainService}

/** Represents the input port manager of the application. */
trait InputPortManager:
  def stationPort: StationPorts.Input
  def routePort: RoutePorts.Input
  def trainPort: TrainPorts.Input
  def timetablePort: TimetablePorts.Input

object InputPortManager:

  /** Creates a new instance of the input port manager. */
  def apply(eventQueue: EventQueue): InputPortManager = new InputPortManagerImpl(eventQueue)

  private case class InputPortManagerImpl(
      eventQueue: EventQueue,
      stationPort: StationPorts.Input,
      routePort: RoutePorts.Input,
      trainPort: TrainPorts.Input,
      timetablePort: TimetablePorts.Input
  ) extends InputPortManager:

    def this(eventQueue: EventQueue) =
      this(
        eventQueue,
        StationService(eventQueue),
        RouteService(eventQueue),
        TrainService(eventQueue),
        TimetableService(eventQueue)
      )
