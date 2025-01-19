package ulisse.applications.adapters

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationPortInputAdapter[N: Numeric, C <: Coordinate[N]](stationManager: StationManager[N, C])
    extends StationPorts.Input[N, C]:
  export stationManager.{addStation, findStationAt, removeStation, stationMap}
