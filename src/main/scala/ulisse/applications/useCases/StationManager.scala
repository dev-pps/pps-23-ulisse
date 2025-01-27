package ulisse.applications.useCases

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](uiPort: StationPorts.Output):
  val stationMap: CheckedStationMap[N, C, S] = StationMap.createCheckedStationMap()
  export stationMap.{addStation, findStationAt, removeStation}
