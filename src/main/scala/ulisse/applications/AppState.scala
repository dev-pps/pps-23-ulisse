package ulisse.applications

import ulisse.applications.managers.SimulationManager
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinates
import ulisse.entities.station.Station

final case class AppState[N: Numeric, C <: Coordinates.Coordinate[N], S <: Station[N, C]](
    stationManager: CheckedStationManager[N, C, S],
    simulationManager: SimulationManager
)
