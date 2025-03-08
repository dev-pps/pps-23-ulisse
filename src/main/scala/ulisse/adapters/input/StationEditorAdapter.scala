package ulisse.adapters.input

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.adapters.input.StationEditorAdapter.StationCreationInfo
import ulisse.applications.ports.StationPorts
import ulisse.applications.ports.StationPorts.Input
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

import scala.concurrent.Future

/** Adapter for the `StationEditorView` that handles interactions between the view and the Station input port. */
trait StationEditorAdapter:
  /** Adds a new station to the application. */
  def addStation(
      stationCreationInfo: StationCreationInfo
  ): Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]]

  /** Updates a station in the application. */
  def updateStation(
      stationCreationInfo: StationCreationInfo,
      oldStation: Station
  ): Future[Either[NonEmptyChain[BaseError], (StationPorts.Input#SM, List[Route])]]

  /** Removes a station from the application. */
  def removeStation(station: Station): Future[Either[NonEmptyChain[BaseError], (StationPorts.Input#SM, List[Route])]]

  /** Finds a station at the given coordinate. */
  def findStationAt(coordinate: Coordinate): Future[Option[Station]]

/** Factory for [[StationEditorAdapter]] instances. */
object StationEditorAdapter:
  /** Information required to create a new station. */
  case class StationCreationInfo(
      stationName: String,
      x: String,
      y: String,
      numberOfPlatforms: String
  )

  /** Creates a new StationEditorAdapter. */
  def apply(appPort: StationPorts.Input): StationEditorAdapter = StationEditorAdapterImpl(appPort)

  /** Represents errors that can occur during view input validation. */
  enum Error extends BaseError:
    case InvalidFirstCoordinateComponentFormat, InvalidSecondCoordinateComponentFormat, InvalidNumberOfPlatformsFormat

  private final case class StationEditorAdapterImpl(private val appPort: StationPorts.Input)
      extends StationEditorAdapter:
    override def addStation(
        stationCreationInfo: StationCreationInfo
    ): Future[Either[NonEmptyChain[BaseError], StationPorts.Input#SM]] =
      stationCreationInfo.createStation(appPort.addStation)

    override def updateStation(
        stationCreationInfo: StationCreationInfo,
        oldStation: Station
    ): Future[Either[NonEmptyChain[BaseError], (StationPorts.Input#SM, List[Route])]] =
      stationCreationInfo.createStation(appPort.updateStation(oldStation, _))

    extension (stationCreationInfo: StationCreationInfo)
      private def createStation[R](
          update: Station => Future[Either[NonEmptyChain[BaseError], R]]
      ): Future[Either[NonEmptyChain[BaseError], R]] =
        createStationFrom(stationCreationInfo) match
          case Left(error)    => Future.successful(Left(error))
          case Right(station) => update(station)

    private def createStationFrom(
        stationCreationInfo: StationCreationInfo
    ): Either[NonEmptyChain[BaseError], Station] = stationCreationInfo match
      case StationCreationInfo(name, x, y, numberOfPlatforms) =>
        (
          x.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidFirstCoordinateComponentFormat),
          y.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidSecondCoordinateComponentFormat),
          numberOfPlatforms.toIntOption.toValidNec(StationEditorAdapter.Error.InvalidNumberOfPlatformsFormat)
        ).mapN((_, _, _)).toEither.flatMap((x, y, nt) => Station.createCheckedStation(name, Coordinate(x, y), nt))

    export appPort.{findStationAt, removeStation}
