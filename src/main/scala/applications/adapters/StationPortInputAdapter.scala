package applications.adapters

import applications.ports.StationPort.Inbound
import applications.station.StationMap
import applications.usecase.StationManager
import entities.Location
import entities.station.Station

final case class StationPortInputAdapter[L <: Location](
    stationManager: StationManager[L]
) extends Inbound[L]:
  override def stationMap: StationMap[L] = stationManager.stationMap

  override def addStation(station: Station[L])
      : Either[StationMap.Error, StationMap[L]] =
    stationManager.addStation(station)

  override def removeStation(station: Station[L])
      : Either[StationMap.Error, StationMap[L]] =
    stationManager.removeStation(station)

  override def findStationAt(location: L): Option[Station[L]] =
    stationManager.findStationAt(location)
