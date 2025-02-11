package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import cats.implicits.catsSyntaxEq
import ulisse.entities.Coordinates.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a Station.
  *
  * A `Station` represents a location where trains can stop. Each station has a name, a location, and a number of
  * tracks.
  *
  * @tparam C
  *   A type that extends `Coordinate[?]`, which represents the station's location.
  */
trait Station[C <: Coordinate[?]]:
  val name: String
  val coordinate: C
  val numberOfTracks: Int
  override def equals(other: Any): Boolean = other match
    case s: Station[_] =>
      name === s.name &&
      coordinate == s.coordinate &&
      numberOfTracks === s.numberOfTracks
    case _ => false

/** Factory for [[Station]] instances. */
object Station:
  /** Creates a `Station` instance.
    *
    * @tparam C
    *   A type that extends `Coordinate[?]`, which represents the station's location.
    * @param name
    *   The name of the station.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks.
    * @return
    *   A `Station` instance.
    */
  def apply[C <: Coordinate[?]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ): Station[C] =
    StationImpl(name, coordinate, numberOfTrack)

  given [C <: Coordinate[?]]: ((String, C, Int) => Either[NonEmptyChain[BaseError], CheckedStation[C]]) =
    Station.createCheckedStation

  /** Creates a `Station` instance with validation.
    *
    * @tparam C
    *   A type that extends `Coordinate[?]`, which represents the station's location.
    * @param name
    *   The name of the station. Must not be empty or blank.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks. Must be greater than 0.
    * @return
    *   Either a `Station` instance or a `NonEmptyChain` of `Errors` indicating the issues.
    */
  def createCheckedStation[C <: Coordinate[?]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ): Either[NonEmptyChain[CheckedStation.Error], CheckedStation[C]] =
    (
      validateNonBlankString(name, CheckedStation.Error.InvalidName).toValidatedNec,
      validatePositive(numberOfTrack, CheckedStation.Error.InvalidNumberOfTrack).toValidatedNec
    ).mapN(CheckedStation(_, coordinate, _)).toEither

  /** Defines a `CheckedStation`.
    *
    * A `CheckedStation` represents a validated station.
    *
    * @tparam C
    *   A type that extends `Coordinate[?]`, which represents the station's location.
    * @param name
    *   The name of the station. Must be non-blank.
    * @param coordinate
    *   The location of the station, represented as a coordinate of type `C`.
    * @param numberOfTracks
    *   The number of tracks at the station. Must be a positive integer.
    *
    * **Note**: Instances of `CheckedStation` can only be created through the `Station.createCheckedStation` method to
    * ensure validation.
    */
  case class CheckedStation[C <: Coordinate[?]] private[Station] (
      name: String,
      coordinate: C,
      numberOfTracks: Int
  ) extends Station[C]

  /** Represents a selectable station.
    *
    * A `SelectableStation` wraps a `Station` instance and provides a boolean flag to indicate whether the station is
    * selected.
    *
    * @tparam C
    *   A type that extends `Coordinate[?]`, representing the station's location.
    * @param station
    *   The station instance of type `Station[C]` to be wrapped.
    * @param selected
    *   A boolean flag indicating whether the station is selected. `true` if selected, `false` otherwise.
    */
  final case class SelectableStation[C <: Coordinate[?]](
      station: Station[C],
      selected: Boolean
  ) extends Station[C] with Selectable:
    export station.*

  private final case class StationImpl[C <: Coordinate[?]](
      name: String,
      coordinate: C,
      numberOfTracks: Int
  ) extends Station[C]

  object CheckedStation:
    /** Represents errors that can occur during station creation. */
    enum Error extends BaseError:
      case InvalidName, InvalidNumberOfTrack

/** Defines a Selectable Object. */
trait Selectable:
  val selected: Boolean
