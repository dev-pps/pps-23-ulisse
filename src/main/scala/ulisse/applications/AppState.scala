package ulisse.applications

import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates
import ulisse.entities.station.Station

final case class AppState[N: Numeric, C <: Coordinates.Coordinate[N], S <: Station[N, C]](
    stationMap: CheckedStationMap[N, C, S]
)
