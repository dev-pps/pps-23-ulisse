package ulisse.adapters

import ulisse.adapters.input.{RouteAdapter, SimulationInfoAdapter, TrainViewAdapter, SimulationPageAdapter, StationEditorAdapter}
import ulisse.applications.InputPortManager

/** Represents the input adapter manager of the application. */
trait InputAdapterManager:
  def trainAdapter: TrainViewAdapter
  /** The station editor adapter. */
  val station: StationEditorAdapter

  /** The route adapter. */
  val route: RouteAdapter

  /** The simulation page adapter. */
  val simulationPage: SimulationPageAdapter

  /** The simulation info adapter. */
  val simulationInfo: SimulationInfoAdapter

/** Companion object of the input adapter manager. */
object InputAdapterManager:

  /** Creates a new instance of the input adapter manager. */
  def apply(ports: InputPortManager, simulationPage: SimulationPageAdapter): InputAdapterManager =
    new InputAdapterManagerImpl(ports, simulationPage)

  private final case class InputAdapterManagerImpl(
      station: StationEditorAdapter,
      route: RouteAdapter,
      trainAdapter: TrainViewAdapter
      simulationPage: SimulationPageAdapter,
      simulationInfo: SimulationInfoAdapter
  ) extends InputAdapterManager:
    def this(ports: InputPortManager, simulationPage: SimulationPageAdapter) =
      this(
        StationEditorAdapter(ports.station),
        RouteAdapter(ports.route),
        TrainViewAdapter(ports.train),
        simulationPage,
        SimulationInfoAdapter(ports.simulationInfo)
      )
