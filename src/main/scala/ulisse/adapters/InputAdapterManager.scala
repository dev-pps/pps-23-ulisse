package ulisse.adapters

import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.adapters.input.*
import ulisse.applications.InputPortManager

/** Represents the input adapter manager of the application. */
trait InputAdapterManager:
  /** The train editor adapter */
  val train: TrainViewAdapter

  /** The timetable editor adapter */
  val timetable: TimetableViewAdapter

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
  def apply(
      ports: InputPortManager,
      simulationAdapter: SimulationPageAdapter,
      simulationInfoAdapter: SimulationInfoAdapter
  ): InputAdapterManager =
    new InputAdapterManagerImpl(ports, simulationAdapter, simulationInfoAdapter)

  /** Creates a new instance of the input adapter manager. */
  def create(
      stations: StationEditorAdapter,
      routes: RouteAdapter,
      trains: TrainViewAdapter,
      timetables: TimetableViewAdapter,
      simulationPage: SimulationPageAdapter,
      simulationInfoAdapter: SimulationInfoAdapter
  ): InputAdapterManager =
    InputAdapterManagerImpl(stations, routes, trains, timetables, simulationPage, simulationInfoAdapter)

  private final case class InputAdapterManagerImpl(
      station: StationEditorAdapter,
      route: RouteAdapter,
      train: TrainViewAdapter,
      timetable: TimetableViewAdapter,
      simulationPage: SimulationPageAdapter,
      simulationInfo: SimulationInfoAdapter
  ) extends InputAdapterManager:
    def this(
        ports: InputPortManager,
        simulationPage: SimulationPageAdapter,
        simulationInfoAdapter: SimulationInfoAdapter
    ) =
      this(
        StationEditorAdapter(ports.station),
        RouteAdapter(ports.route),
        TrainViewAdapter(ports.train),
        TimetableViewAdapter(ports.timetable, ports.train),
        simulationPage,
        simulationInfoAdapter
      )
