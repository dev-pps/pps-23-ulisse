package ulisse.adapters.input

import cats.data.{Chain, NonEmptyChain}
import cats.syntax.all.*
import ulisse.applications.ports.StationPorts
import ulisse.applications.ports.StationPorts.Input
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

import scala.concurrent.Future

object StationEditorAdapter:

  /** Represents errors that can occur during view input validation. */
  enum Error extends BaseError:
    case InvalidFirstCoordinateComponentFormat, InvalidSecondCoordinateComponentFormat, InvalidNumberOfTrackFormat

/** Controller for the `StationEditorView`, managing interactions between the view and the application. */
final case class StationEditorAdapter(
    private val appPort: StationPorts.Input
):

  def addStation(
      stationName: String,
      x: String,
      y: String,
      numberOfTrack: String
  ): Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]] =
    createStation(stationName, x, y, numberOfTrack) match
      case Left(error)    => Future.successful(Left(error))
      case Right(station) => appPort.addStation(station)

  def updateStation(
      stationName: String,
      x: String,
      y: String,
      numberOfTrack: String,
      oldStation: Station
  ): Future[Either[NonEmptyChain[BaseError], (StationPorts.Input#SM, List[Route])]] =
    createStation(stationName, x, y, numberOfTrack) match
      case Left(error)    => Future.successful(Left(error))
      case Right(station) => appPort.updateStation(oldStation, station)

  /** Handles the click event when the "OK" button is pressed.
    *
    * This method attempts to create and add a new station using the provided information.
    * If an `oldStation` is given, the station is instead updated.
    * If the input data or it's format is invalid errors are returned.
    */
  def onOkClick(
      stationName: String,
      x: String,
      y: String,
      numberOfTrack: String,
      oldStation: Option[Station]
  ): Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]] =
    createStation(stationName, x, y, numberOfTrack) match
      case Left(error)    => Future.successful(Left(error))
      case Right(station) => appPort.addStation(station)

  private def createStation(
      name: String,
      x: String,
      y: String,
      numberOfTrack: String
  ): Either[NonEmptyChain[BaseError], Station] =
    (
      x.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidFirstCoordinateComponentFormat),
      y.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidSecondCoordinateComponentFormat),
      numberOfTrack.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidNumberOfTrackFormat)
    ).mapN((_, _, _)).toEither.flatMap((x, y, nt) => Station.createCheckedStation(name, Coordinate(x, y), nt))

  export appPort.{findStationAt, removeStation}
