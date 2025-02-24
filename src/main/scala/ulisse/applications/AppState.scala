package ulisse.applications

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

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
)
