package ulisse.applications

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.infrastructures.commons.TimeProviders.TimeProvider
import ulisse.utils.Times.Time

object AppState:
  def default(): AppState =
    AppState(
      StationManager(),
      RouteManager.empty(),
      TrainManager(List.empty),
      TechnologyManager(List.empty),
      SimulationManager.emptyBatchManager(TimeProviderAdapter(TimeProvider.systemTimeProvider()))
    )

final case class AppState(
    stationManager: StationManager,
    routeManager: RouteManager,
    trainManager: TrainManager,
    technologyManager: TechnologyManager[TrainTechnology],
    simulationManager: SimulationManager
):
  def swap(f: AppState => StationManager | RouteManager | TrainManager | TechnologyManager[
    TrainTechnology
  ] | SimulationManager): AppState = f(this) match
    case s: StationManager                     => copy(stationManager = s)
    case r: RouteManager                       => copy(routeManager = r)
    case t: TrainManager                       => copy(trainManager = t)
    case t: TechnologyManager[TrainTechnology] => copy(technologyManager = t)
    case s: SimulationManager                  => copy(simulationManager = s)
