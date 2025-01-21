package ulisse.applications.useCases

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation

final case class StationManager[N: Numeric, C <: Coordinate[N]](uiPort: StationPorts.Output):
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: CheckedStationMap[N, C] = StationMap.createCheckedStationMap[N, C]()

  def stationMap: StationMap[N, C] = _stationMap

  def addStation(station: CheckedStation[N, C]): Either[CheckedStationMap.Error, CheckedStationMap[N, C]] =
    _stationMap.addStation(station).map(updatedMap =>
      _stationMap = updatedMap; updatedMap
    )

  def removeStation(station: Station[N, C]): Either[CheckedStationMap.Error, CheckedStationMap[N, C]] =
    _stationMap.removeStation(station).map: updatedMap =>
      _stationMap = updatedMap; updatedMap

  def findStationAt(location: C): Option[Station[N, C]] =
    _stationMap.findStationAt(location)
