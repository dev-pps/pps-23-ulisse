package ulisse.applications

import ulisse.applications.ports.*
import ulisse.applications.useCases.*

/** Represents the input port manager of the application. */
trait InputPortManager:
  /** The station input port. */
  val station: StationPorts.Input

  /** The route input port. */
  val route: RoutePorts.Input

  /** The simulation input port. */
  val train: TrainPorts.Input

  /** The timetable input port. */
  val timetable: TimetablePorts.Input

  /** The simulation input port. */
  val simulation: SimulationPorts.Input

  /** The simulation info input port. */
  val simulationInfo: SimulationInfoPorts.Input

/** Companion object of the input port manager. */
object InputPortManager:

  /** Creates a new instance of the input port manager. */
  def apply(
      eventQueue: EventQueue,
      simulationInput: SimulationPorts.Input,
      simulationInfo: SimulationInfoPorts.Input
  ): InputPortManager =
    new InputPortManagerImpl(eventQueue, simulationInput, simulationInfo)

  private case class InputPortManagerImpl(
      eventQueue: EventQueue,
      station: StationPorts.Input,
      route: RoutePorts.Input,
      train: TrainPorts.Input,
      timetable: TimetablePorts.Input,
      simulation: SimulationPorts.Input,
      simulationInfo: SimulationInfoPorts.Input
  ) extends InputPortManager:

    def this(
        eventQueue: EventQueue,
        simulationInput: SimulationPorts.Input,
        simulationInfo: SimulationInfoPorts.Input
    ) =
      this(
        eventQueue,
        StationService(eventQueue),
        RouteService(eventQueue),
        TrainService(eventQueue),
        TimetableService(eventQueue),
        simulationInput,
        simulationInfo
      )
