package model

import cats.implicits.catsSyntaxEq
import station.*

final case class Model():

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _stationMap: StationMap = StationMap(List[Station]())

  def stationMap: Seq[Station] = _stationMap.stations
  def addStation(station: Station): Unit =
    _stationMap = StationMap(station +: _stationMap.stations)
    println(_stationMap)

  def removeStation(station: Station): Unit =
    val updatedStations =
      _stationMap.stations.filterNot(_.location === station.location)
    _stationMap = StationMap(updatedStations)

  def updateStation(station: Station): Unit =
    removeStation(station)
    addStation(station)
