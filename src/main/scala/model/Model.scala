package model

import cats.implicits.catsSyntaxEq
import model.station.Location.Location
import station.*

final case class Model():

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: StationMap = StationMap(List[Station]())

  def stationMap: Seq[Station] = _stationMap.stations

  def addStation(station: Station): Unit =
    _stationMap = StationMap(station +: _stationMap.stations)

  def removeStation(station: Station): Unit =
    val updatedStations =
      _stationMap.stations.filterNot(_.location === station.location)
    _stationMap = StationMap(updatedStations)

  def findStationAt(location: Location): Option[Station] =
    _stationMap.find(_.location === location)
