package ulisse.applications.useCases

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationManager[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](uiPort: StationPorts.Output):
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: CheckedStationMap[N, C, S] = StationMap.createCheckedStationMap()

  def stationMap: CheckedStationMap[N, C, S] = _stationMap

  def addStation(station: S): Either[CheckedStationMap.Error, CheckedStationMap[N, C, S]] =
    _stationMap.addStation(station).map(updatedMap =>
      _stationMap = updatedMap; updatedMap
    )

  def removeStation(station: S): Either[CheckedStationMap.Error, CheckedStationMap[N, C, S]] =
    _stationMap.removeStation(station).map: updatedMap =>
      _stationMap = updatedMap; updatedMap

  def findStationAt(location: C): Option[S] =
    _stationMap.findStationAt(location)
