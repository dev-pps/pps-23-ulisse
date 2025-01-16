package applications.usecase

import applications.ports.StationPort
import applications.station.StationMap
import entities.Location
import entities.station.Station

final case class StationManager[L <: Location](uiPort: StationPort.Outbound):
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: StationMap[L] = StationMap[L]()

  def stationMap: StationMap[L] = _stationMap

  def addStation(station: Station[L]): Either[StationMap.Error, StationMap[L]] =
    println(_stationMap)
    _stationMap.addStation(station).map: updatedMap =>
      println(updatedMap)
      _stationMap = updatedMap;
      println(stationMap)
      updatedMap

  def removeStation(station: Station[L])
      : Either[StationMap.Error, StationMap[L]] =
    _stationMap.removeStation(station).map: updatedMap =>
      _stationMap = updatedMap; updatedMap

  def findStationAt(location: L): Option[Station[L]] =
    _stationMap.findStationAt(location)
