package controllers.station

import model.Model
import model.station.Location.Location
import model.station.{Location, Station}
import views.*
import views.station.StationEditorView

/** Controller for StationEditorView.
  *
  * @constructor
  *   create a new StationEditorController with a view and model.
  * @param view
  *   the related StationEditorView
  * @param model
  *   the application model
  */
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

  /** Handles the click event when the "OK" button is pressed.
    *
    * This method is responsible for creating a new station with the provided
    * information. If an `oldStation` is provided, it removes the old station
    * before adding the newly created station to the model. If the information
    * provided is invalid, IllegalArgumentException is thrown.
    *
    * @param stationName
    *   The name of the new station.
    * @param latitude
    *   The latitude of the new station's location.
    * @param longitude
    *   The longitude of the new station's location.
    * @param numberOfTrack
    *   The number of tracks at the new station.
    * @param oldStation
    *   An optional existing station that may be removed before adding the new
    *   station. If no station is provided, no removal occurs.
    * @throws IllegalArgumentException
    *   If the information provided is invalid.
    */
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
