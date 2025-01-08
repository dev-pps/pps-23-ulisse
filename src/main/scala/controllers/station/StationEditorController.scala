package controllers.station

import model.Model
import model.station.Location.Location
import model.station.{Location, Station}
import views.*
import views.station.StationEditorView

final case class StationEditorController(view: StationEditorView, model: Model):
  private def createStation(
      name: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String
  ): Station =
    Station(
      name,
      Location(latitude.toDouble, longitude.toDouble),
      numberOfTrack.toInt
    )

  def onOkClick(
      stationName: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String,
      oldStation: Option[Station]
  ): Unit =
    val newStation = createStation(
      stationName,
      latitude,
      longitude,
      numberOfTrack
    )
    for s <- oldStation do removeStation(s)
    model.addStation(newStation)

  export model.removeStation, model.findStationAt
