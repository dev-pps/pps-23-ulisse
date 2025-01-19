package ulisse.applications.adapters

import ulisse.applications.ports.StationPort
import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Location
import ulisse.entities.station.Station

final case class StationPortInputAdapter[L <: Location](stationManager: StationManager[L]) extends StationPort.Input[L]:
  export stationManager.{addStation, findStationAt, removeStation, stationMap}
