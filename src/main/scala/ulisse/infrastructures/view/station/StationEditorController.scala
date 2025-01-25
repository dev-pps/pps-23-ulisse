package ulisse.infrastructures.view.station

import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.{Coordinate, Geo, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.utils.Errors.BaseError

object StationEditorController:

  enum Error extends BaseError:
    case InvalidRowFormat, InvalidColumnFormat, InvalidNumberOfTrackFormat

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
final case class StationEditorController[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    appPort: StationPorts.Input[N, C, S]
):

  private def createStation(
      name: String,
      latitude: String,
      longitude: String,
      numberOfTrack: String,
      coordinateGenerator: (N, N) => Either[BaseError, C],
      stationGenerator: (String, C, Int) => Either[BaseError, S]
  )(using numeric: Numeric[N]): Either[BaseError, S] =
    for
      latitude      <- numeric.parseString(latitude).toRight(StationEditorController.Error.InvalidRowFormat)
      longitude     <- numeric.parseString(longitude).toRight(StationEditorController.Error.InvalidColumnFormat)
      coordinate    <- coordinateGenerator(latitude, longitude)
      numberOfTrack <- numberOfTrack.toIntOption.toRight(StationEditorController.Error.InvalidNumberOfTrackFormat)
      station       <- stationGenerator(name, coordinate, numberOfTrack)
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
      oldStation: Option[S]
  )(using coordinateGenerator: (N, N) => Either[BaseError, C])(using
      stationGenerator: (String, C, Int) => Either[BaseError, S]
  ): Either[BaseError, StationMap[N, C, S]] =
    createStation(stationName, latitude, longitude, numberOfTrack, coordinateGenerator, stationGenerator).flatMap { s =>
      for old <- oldStation do removeStation(old)
      val st = appPort.addStation(s)
      println(st)
      st
    } match
      case Right(stationMap) => Right(stationMap)
      case Left(error)       => Left(error)

  export appPort.{findStationAt, removeStation}
