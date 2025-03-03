package ulisse.applications

import ulisse.applications.ports.{RoutePorts, SimulationPorts, StationPorts, TimetablePorts, TrainPorts}
import ulisse.applications.useCases.{RouteService, SimulationService, StationService, TimetableService, TrainService}

trait InputPortManager

object InputPortManager:

  def apply(eventQueue: EventQueue): InputPortManager = new InputPortManagerImpl(eventQueue)

  private case class InputPortManagerImpl(
      eventQueue: EventQueue,
      stationService: StationPorts.Input,
      routeService: RoutePorts.Input,
      trainService: TrainPorts.Input,
      timetableService: TimetablePorts.Input
  ) extends InputPortManager:

    def this(eventQueue: EventQueue) =
      this(
        eventQueue,
        StationService(eventQueue),
        RouteService(eventQueue),
        TrainService(eventQueue),
        TimetableService(eventQueue)
      )
