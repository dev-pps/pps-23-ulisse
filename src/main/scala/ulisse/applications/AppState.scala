package ulisse.applications

import ulisse.applications.managers.{CheckedStationManager, SimulationManager}
import ulisse.entities.station.Station

final case class AppState[S <: Station[?]](
    stationManager: CheckedStationManager[S]
)
final case class SimulationState(simulationManager: SimulationManager)
