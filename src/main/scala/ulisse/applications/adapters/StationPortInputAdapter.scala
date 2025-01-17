package ulisse.applications.adapters

import ulisse.applications.ports.StationPort.Inbound
import ulisse.applications.station.StationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Location
import ulisse.entities.Location.Grid
import ulisse.entities.station.Station

//final case class GridStationPortInputAdapter(
//    stationManager: StationManager[Grid]
//) extends Inbound:
//  override def stationMap: Seq[(String, String, String, String)] = stationManager.stationMap.stations.map {
//    station => (station.name, station.location.row.toString, station.location.column.toString, station.numberOfTrack.toString)
//  }
//
//  override def addStation(name: String, x: String, y: String, numberOfTracks: String): Either[String, List[(String, String, String, String)]] =
//    stationManager.addStation(Location.createGrid(x.toInt, y.toInt), name, numberOfTracks.toInt).map {
//      station => stationManager.stationMap.stations.map {
//        station => (station.name, station.location.row.toString, station.location.column.toString, station.numberOfTrack.toString)
//      }
//    }
//
//  override def removeStation(station: Station[Grid]): Either[StationMap.Error, StationMap[L]] =
//    stationManager.removeStation(station)

//  override def findStationAt(row: String, column: String): Option[(String, String, String, String)] =
//    Option(("Null", "Null", "Null", "Null"))
//    stationManager.findStationAt(location)
