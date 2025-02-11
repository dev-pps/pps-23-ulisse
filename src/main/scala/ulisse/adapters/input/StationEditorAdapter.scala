package ulisse.adapters.input

import cats.data.{Chain, NonEmptyChain}
import cats.syntax.all.*
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.managers.{CheckedStationManager, StationManager}
import ulisse.applications.ports.StationPorts
import ulisse.applications.ports.StationPorts.Input
import ulisse.entities.Coordinates.{Coordinate, Geo, Grid}
import ulisse.entities.station.Station
import ulisse.entities.station.Station.CheckedStation
import ulisse.utils.Errors.BaseError

import java.util.concurrent.Executors
import scala.concurrent.impl.Promise
import scala.concurrent.{ExecutionContext, Future}

object StationEditorAdapter:

  enum Error extends BaseError:
    case InvalidFirstCoordinateComponentFormat, InvalidSecondCoordinateComponentFormat, InvalidNumberOfTrackFormat

/** Controller for the `StationEditorView`, managing interactions between the view and the application.
  *
  * @tparam N
  *   The numeric type representing the station's coordinates (e.g., `Int`, `Double`).
  *   - An instance of `Numeric` must be available for `N`.
  * @tparam C
  *   A subtype of `Coordinate[N]` representing the station's location.
  *   - `C` must support coordinate comparison and uniqueness.
  * @tparam S
  *   A subtype of `Station[N, C]`, representing the station model.
  * @param appPort
  *   The `StationInputPort` to interact with the application.
  */
final case class StationEditorAdapter[N: Numeric, C <: Coordinate[N], S <: Station[C]](
    appPort: StationPorts.Input[S]
):

  /** Handles the click event when the "OK" button is pressed.
    *
    * This method attempts to create a new station using the provided information. If an `oldStation` is given, it is
    * removed before adding the newly created station. If the input data is invalid, a `NonEmptyChain` of `BaseError` is
    * returned.
    *
    * @param stationName
    *   The name of the new station.
    * @param x
    *   The first coordinate component of the station's location (as a `String`).
    * @param y
    *   The second coordinate component of the station's location (as a `String`).
    * @param numberOfTrack
    *   The number of tracks at the new station (as a `String`).
    * @param oldStation
    *   An optional existing station to be removed before adding the new one. If `None`, no removal occurs.
    * @param coordinateGenerator
    *   A function that converts coordinate values (`N`, `N`) into a valid `C` coordinate, returning either a
    *   `NonEmptyChain` of errors or a valid coordinate.
    * @param stationGenerator
    *   A function that creates a `Station` instance from its name, coordinates, and track count, returning either a
    *   `NonEmptyChain` of errors or a valid station.
    * @return
    *   A `Future` containing either:
    *   - `Left(NonEmptyChain[BaseError])` if validation fails.
    *   - `Right(StationMap[S])` if the station is successfully created and added.
    */
  def onOkClick(
      stationName: String,
      x: String,
      y: String,
      numberOfTrack: String,
      oldStation: Option[Station[?]]
  )(using
      coordinateGenerator: (N, N) => Either[NonEmptyChain[BaseError], C],
      stationGenerator: (String, C, Int) => Either[NonEmptyChain[BaseError], S]
  ): Future[Either[NonEmptyChain[BaseError], StationPorts.Input[S]#SM]] =
    createStation(stationName, x, y, numberOfTrack, coordinateGenerator, stationGenerator) match
      case Left(error) => Future.successful(Left(error))
      case Right(station) => oldStation match
          case Some(oldStation) => appPort.updateStation(oldStation, station)
          case None             => appPort.addStation(station)

  private def createStation(
      name: String,
      x: String,
      y: String,
      numberOfTrack: String,
      coordinateGenerator: (N, N) => Either[NonEmptyChain[BaseError], C],
      stationGenerator: (String, C, Int) => Either[NonEmptyChain[BaseError], S]
  )(using numeric: Numeric[N]): Either[NonEmptyChain[BaseError], S] =
    val validatedCoordinate = (
      numeric.parseString(x).toValidNec(StationEditorAdapter.Error.InvalidFirstCoordinateComponentFormat),
      numeric.parseString(y).toValidNec(StationEditorAdapter.Error.InvalidSecondCoordinateComponentFormat)
    ).mapN((_, _)).toEither.flatMap(coordinateGenerator(_, _))
    val validatedNumberOfTrack =
      numberOfTrack.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidNumberOfTrackFormat).toEither
    (validatedCoordinate.toValidated, validatedNumberOfTrack.toValidated)
      .mapN((_, _)).toEither.flatMap(stationGenerator(name, _, _))

  export appPort.{findStationAt, removeStation}
