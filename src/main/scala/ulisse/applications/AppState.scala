package ulisse.applications

import ulisse.applications.managers.StationManager
import ulisse.entities.station.Station

final case class AppState(
    stationManager: StationManager
)
