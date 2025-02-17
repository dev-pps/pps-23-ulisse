package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.implicits.catsSyntaxEq
import cats.syntax.all.*
import ulisse.entities.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a `Station`, a place where trains can stop. */
trait Station:
  val id: Int
  val name: String
  val coordinate: Coordinate
  val numberOfTracks: Int
  override def equals(other: Any): Boolean = other match
    case s: Station =>
      name === s.name &&
      coordinate == s.coordinate &&
      numberOfTracks == s.numberOfTracks
    case _ => false

trait StationEnvironmentElement extends Station:
  val tracks: List[Track]

/** Factory for [[Station]] instances. */
object Station:
  /** Creates a `Station` instance. The resulting station has at least 1 track. */
  def apply(name: String, coordinate: Coordinate, numberOfTrack: Int): Station =
    val minNumberOfTracks = 1
    StationImpl(name, coordinate, math.max(minNumberOfTracks, numberOfTrack))

  /** Creates a `Station` instance with validation. The resulting station must have non-blank name and positive numberOfTrack */
  def createCheckedStation(
      name: String,
      coordinate: Coordinate,
      numberOfTrack: Int
  ): Either[NonEmptyChain[Station.Error], Station] =
    (
      validateNonBlankString(name, Station.Error.InvalidName).toValidatedNec,
      validatePositive(numberOfTrack, Station.Error.InvalidNumberOfTrack).toValidatedNec
    ).mapN(Station(_, coordinate, _)).toEither

  private final case class StationImpl(name: String, coordinate: Coordinate, numberOfTracks: Int) extends Station:
    val id: Int = hashCode()

  def createStationEnvironmentElement(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station)

  private final case class StationEnvironmentElementImpl(station: Station) extends StationEnvironmentElement:
    export station.*
    val tracks: List[Track] = Track.generateSequentialTracks(numberOfTracks)

  /** Represents errors that can occur during `Station` creation. */
  enum Error extends BaseError:
    case InvalidName, InvalidNumberOfTrack
