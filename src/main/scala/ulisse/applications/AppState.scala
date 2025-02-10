package ulisse.applications

import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.entities.Coordinates
import ulisse.entities.station.Station

final case class AppState[S <: Station[?]](
    stationManager: CheckedStationManager[S]
)
