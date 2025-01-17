package ulisse.applications.station.ports

import ulisse.entities.Location
import ulisse.entities.station.Station

object StationPort:
  trait Inbound:
    def addStation[L <: Location](station: Station[L]): Unit

    def removeStation[L <: Location](station: Station[L]): Unit

    def findStationAt[L <: Location](location: L): Option[Station[L]]
