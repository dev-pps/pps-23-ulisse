package ulisse.applications

import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinates
import ulisse.entities.station.Station

final case class AppState[N: Numeric, C <: Coordinates.Coordinate[N], S <: Station[N, C]](
    stationMap: CheckedStationManager[N, C, S]
)
