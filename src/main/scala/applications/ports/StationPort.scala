package applications.ports

import applications.station.StationMap
import entities.Location
import entities.station.Station

object StationPort:
  trait Inbound[L <: Location]:

    def stationMap: StationMap[L]

    def addStation(station: Station[L]): Either[StationMap.Error, StationMap[L]]

    def removeStation(station: Station[L])
        : Either[StationMap.Error, StationMap[L]]

    def findStationAt(location: L): Option[Station[L]]
