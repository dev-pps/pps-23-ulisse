package ulisse.infrastructures.view.station

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.{Coordinate, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.utils.Errors.BaseError

/** Controller for StationEditorView.
  *
  * @constructor
  *   create a new StationEditorController with a view and model.
  * @param appPort
  *   the 'StationInputPort' to interact with the application
  * @tparam N
  *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
  *   - An instance of `Numeric` must be available for the `N` type.
  * @tparam C
  *   A type that extends `Coordinate[N]`, which represents the station's location.
  *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
  */
final case class StationEditorController(appPort: StationPorts.Input[Int, Grid]):

  enum Error extends BaseError:
    case InvalidRowFormat, InvalidColumnFormat, InvalidNumberOfTrackFormat

  private[this] def createStation(
      name: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String
  ): Either[BaseError, CheckedStation[Int, Grid]] =
    for
      row           <- latitude.toIntOption.toRight(Error.InvalidRowFormat)
      column        <- longitude.toIntOption.toRight(Error.InvalidColumnFormat)
      coordinate    <- Coordinate.createGrid(row, column)
      numberOfTrack <- numberOfTrack.toIntOption.toRight(Error.InvalidNumberOfTrackFormat)
      station       <- Station.createCheckedStation(name, coordinate, numberOfTrack)
    yield station

  /** Handles the click event when the "OK" button is pressed.
    *
    * This method is responsible for creating a new station with the provided information. If an `oldStation` is
    * provided, it removes the old station before adding the newly created station to the model. If the information
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
    *   An optional existing station that may be removed before adding the new station. If no station is provided, no
    *   removal occurs.
    * @throws IllegalArgumentException
    *   If the information provided is invalid.
    */
  def onOkClick(
      stationName: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String,
      oldStation: Option[Station[Int, Grid]]
  ): Either[BaseError, StationMap[Int, Grid]] =
    createStation(stationName, latitude, longitude, numberOfTrack).flatMap {
      for old <- oldStation do removeStation(old)
      appPort.addStation(_)
    } match
      case Right(stationMap) => println(stationMap); Right(stationMap)
      case Left(error)       => Left(error)

  export appPort.{findStationAt, removeStation}
