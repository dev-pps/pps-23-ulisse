package ulisse.applications

import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates
import ulisse.entities.station.Station

final case class AppState[N: Numeric, C <: Coordinates.Coordinate[N], S <: Station[N, C]](
    stationManager: StationManager[N, C, S]
)
