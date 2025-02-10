package ulisse.applications

import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.station.Station

final case class AppState(
    stationManager: StationManager
)
final case class SimulationState(simulationManager: SimulationManager)
