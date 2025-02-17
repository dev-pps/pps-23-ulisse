package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.implicits.catsSyntaxEq
import cats.syntax.all.*
import ulisse.entities.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a Station.
  *
  * A `Station` is a location where trains can stop. Each station has a name, a location, and a number of
  * tracks.
  */
trait Station:
  val name: String
  val coordinate: Coordinate
  val numberOfTracks: Int
  override def equals(other: Any): Boolean = other match
    case s: Station =>
      name === s.name &&
      coordinate == s.coordinate &&
      numberOfTracks == s.numberOfTracks
    case _ => false

/** Factory for [[Station]] instances. */
object Station:
  /** Creates a `Station` instance.
    *
    * @param name
    *   The name of the station.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks.
    * @return
    *   A `Station` instance.
    */
  def apply(name: String, coordinate: Coordinate, numberOfTrack: Int): Station =
    val minNumberOfTracks = 1
    StationImpl(name, coordinate, math.max(minNumberOfTracks, numberOfTrack))

  /** Creates a `Station` instance with validation.
    *
    * @param name
    *   The name of the station. Must not be empty or blank.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks. Must be greater than 0.
    * @return
    *   Either a `Station` instance or a `NonEmptyChain` of `Errors` indicating the issues.
    */
  def createCheckedStation(
      name: String,
      coordinate: Coordinate,
      numberOfTrack: Int
  ): Either[NonEmptyChain[Station.Error], Station] =
    (
      validateNonBlankString(name, Station.Error.InvalidName).toValidatedNec,
      validatePositive(numberOfTrack, Station.Error.InvalidNumberOfTrack).toValidatedNec
    ).mapN(Station(_, coordinate, _)).toEither

  private final case class StationImpl(name: String, coordinate: Coordinate, numberOfTracks: Int) extends Station

  /** Represents errors that can occur during station creation. */
  enum Error extends BaseError:
    case InvalidName, InvalidNumberOfTrack
