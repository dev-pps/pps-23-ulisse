package ulisse.applications.useCases

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationManager[N: Numeric, C <: Coordinate[N]](uiPort: StationPorts.Output):
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: StationMap[N, C] = StationMap[N, C]()

  def stationMap: StationMap[N, C] = _stationMap

  def addStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]] =
    _stationMap.addStation(station).map: updatedMap =>
      _stationMap = updatedMap; updatedMap

  def removeStation(station: Station[N, C]): Either[StationMap.Error, StationMap[N, C]] =
    _stationMap.removeStation(station).map: updatedMap =>
      _stationMap = updatedMap; updatedMap

  def findStationAt(location: C): Option[Station[N, C]] =
    _stationMap.findStationAt(location)
