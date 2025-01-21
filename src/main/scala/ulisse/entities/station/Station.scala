package ulisse.entities.station

import cats.implicits.catsSyntaxEq
import ulisse.entities.Coordinates.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a Station.
  *
  * A `Station` represents a location where trains can stop. Each station has a name, a location, and a number of
  * tracks.
  *
  * @tparam N
  *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
  *   - An instance of `Numeric` must be available for the `N` type.
  * @tparam C
  *   A type that extends `Coordinate[N]`, which represents the station's location.
  *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
  */
trait Station[N: Numeric, C <: Coordinate[N]]:
  val name: String
  val coordinate: C
  val numberOfTracks: Int
  override def equals(other: Any): Boolean = other match
    case s: Station[_, _] =>
      name === s.name &&
      coordinate == s.coordinate &&
      numberOfTracks === s.numberOfTracks
    case _ => false

/** Factory for [[Station]] instances. */
object Station:
  /** Creates a `Station` instance.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
    *   - An instance of `Numeric` must be available for the `N` type.
    * @tparam C
    *   A type that extends `Coordinate[N]`, which represents the station's location.
    *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
    * @param name
    *   The name of the station.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks.
    * @return
    *   A `Station` instance.
    */
  def apply[N: Numeric, C <: Coordinate[N]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ): Station[N, C] =
    StationImpl(name, coordinate, numberOfTrack)

  /** Creates a `Station` instance with validation.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
    *   - An instance of `Numeric` must be available for the `N` type.
    * @tparam C
    *   A type that extends `Coordinate[N]`, which represents the station's location.
    *   - The `C` type must provide a way to compare coordinates and ensure uniqueness.
    * @param name
    *   The name of the station. Must not be empty or blank.
    * @param coordinate
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks. Must be greater than 0.
    * @return
    *   Either a `Station` instance or an `Errors` indicating the issue.
    */
  def createCheckedStation[N: Numeric, C <: Coordinate[N]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ): Either[CheckedStation.Error, CheckedStation[N, C]] =
    for
      validName          <- validateNonBlankString(name, CheckedStation.Error.InvalidName)
      validNumberOfTrack <- validatePositive(numberOfTrack, CheckedStation.Error.InvalidNumberOfTrack)
    yield CheckedStation(validName, coordinate, validNumberOfTrack)

  private final case class StationImpl[N: Numeric, C <: Coordinate[N]](
      name: String,
      coordinate: C,
      numberOfTracks: Int
  ) extends Station[N, C]

  object CheckedStation:
    /** Represents errors that can occur during station creation. */
    enum Error extends BaseError:
      case InvalidName, InvalidNumberOfTrack

  /** Defines a `CheckedStation`.
    *
    * A `CheckedStation` represents a validated station.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
    *   - An instance of `Numeric` must be available for the `N` type.
    * @tparam C
    *   A type that extends `Coordinate[N]`, which represents the station's location.
    *   - The `C` type must ensure uniqueness and comparability of coordinates.
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
  case class CheckedStation[N: Numeric, C <: Coordinate[N]] private[Station] (
      name: String,
      coordinate: C,
      numberOfTracks: Int
  ) extends Station[N, C]

  /** Represents a selectable station.
    *
    * A `SelectableStation` wraps a `Station` instance and provides a boolean flag to indicate whether the station is
    * selected.
    *
    * @tparam N
    *   The numeric type representing the coordinates of the station (e.g., `Int`, `Double`).
    * @tparam C
    *   A type that extends `Coordinate[N]`, representing the station's location.
    * @param station
    *   The station instance of type `Station[N, C]` to be wrapped.
    * @param selected
    *   A boolean flag indicating whether the station is selected. `true` if selected, `false` otherwise.
    */
  final case class SelectableStation[N: Numeric, C <: Coordinate[N]](
      station: Station[N, C],
      selected: Boolean
  ) extends Station[N, C] with Selectable:
    export station.*

/** Defines a Selectable Object. */
trait Selectable:
  val selected: Boolean
