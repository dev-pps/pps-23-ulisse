package ulisse.infrastructures.view.station

//package applications.station
//
//import cats.syntax.either.*
//import entities.Location.*
//import entities.station.Station
//import entities.Location
//import infrastructures.ui.station.StationEditorView
//
///** Controller for StationEditorView.
//  *
//  * @constructor
//  *   create a new StationEditorController with a view and model.
//  * @param view
//  *   the related StationEditorView
//  * @param model
//  *   the application model
//  */
//final case class StationEditorController(
//    view: StationEditorView,
//    model: Manger
//):
//
//  enum Error:
//    case InvalidRow, InvalidColumn, InvalidNumberOfTrack
//
//  private def createStation(
//      name: String,
//      latitude: String,
//      longitude: String,
//      numberOfTrack: String
//  ): Either[Error, Station[Grid]] = Left(Error.InvalidRow)
//  for
//    row    <- latitude.toIntOption.toRight(Error.InvalidRow)
//    column <- longitude.toIntOption.toRight(Error.InvalidColumn)
//    location <- Location.createGrid(row, column) match
//      case Left(value) => value match
//          case Location.InvalidRow    => Left(Error.InvalidRow)
//          case Location.InvalidColumn => Left(Error.InvalidColumn)
//      case Right(value) => Right(value)
//    numberOfTrack <-
//      numberOfTrack.toIntOption.toRight(Error.InvalidNumberOfTrack)
//  yield Station(name, location, numberOfTrack)
//
//  /** Handles the click event when the "OK" button is pressed.
//    *
//    * This method is responsible for creating a new station with the provided
//    * information. If an `oldStation` is provided, it removes the old station
//    * before adding the newly created station to the model. If the information
//    * provided is invalid, IllegalArgumentException is thrown.
//    *
//    * @param stationName
//    *   The name of the new station.
//    * @param latitude
//    *   The latitude of the new station's location.
//    * @param longitude
//    *   The longitude of the new station's location.
//    * @param numberOfTrack
//    *   The number of tracks at the new station.
//    * @param oldStation
//    *   An optional existing station that may be removed before adding the new
//    *   station. If no station is provided, no removal occurs.
//    * @throws IllegalArgumentException
//    *   If the information provided is invalid.
//    */
//  def onOkClick(
//      stationName: String,
//      latitude: String,
//      longitude: String,
//      numberOfTrack: String,
//      oldStation: Option[Station[Grid]]
//  ): Option[Error] =
//    createStation(stationName, latitude, longitude, numberOfTrack) match {
//      case Left(error) =>
//        Some(error) // Return the error as a String inside Some
//      case Right(station) =>
//        for old <- oldStation do removeStation(old)
//        model.addStation(station)
//        None
//    }
//
//  export model.removeStation, model.findStationAt
