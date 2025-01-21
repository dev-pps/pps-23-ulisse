package ulisse.applications.adapters

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationPortInputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    stationManager: StationManager[N, C, S]
) extends StationPorts.Input[N, C, S]:
  export stationManager.{addStation, findStationAt, removeStation, stationMap}
