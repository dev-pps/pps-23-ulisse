package ulisse.applications

import ulisse.applications.managers.CheckedStationManager
import ulisse.entities.station.Station

final case class AppState[S <: Station[?]](
    stationManager: CheckedStationManager[S]
)
