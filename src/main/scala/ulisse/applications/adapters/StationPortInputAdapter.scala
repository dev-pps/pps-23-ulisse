package ulisse.applications.adapters

import ulisse.applications.AppState
import ulisse.applications.ports.StationPorts
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue

final case class StationPortInputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    stationManager: StationManager[N, C, S]
)(using eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]) extends StationPorts.Input[N, C, S]:
  export stationManager.{addStation, findStationAt, removeStation, stationMap}
