package applications.station.ports

import entities.Location
import entities.station.Station

object StationPort:
  trait Inbound:
    def addStation[L <: Location](station: Station[L]): Unit

    def removeStation[L <: Location](station: Station[L]): Unit

    def findStationAt[L <: Location](location: L): Option[Station[L]]
