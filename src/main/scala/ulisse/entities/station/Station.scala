package ulisse.entities.station

import ulisse.entities.Coordinates.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a Station.
  *
  * A Station represents a location with tracks and associated metadata.
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
  val numberOfTrack: Int

/** Factory for [[Station]] instances. */
object Station:

  def apply[N: Numeric, C <: Coordinate[N]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ): Station[N, C] =
    StationImpl(name, coordinate, numberOfTrack)

  /** Creates a `Station` instance with validation.
    *
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
      numberOfTrack: Int
  ) extends Station[N, C]

  object CheckedStation:
    /** Represents errors that can occur during station creation. */
    enum Error extends BaseError:
      case InvalidName, InvalidNumberOfTrack

  /** Defines a CheckedStation.
    *
    * **Requirements**:
    *   - The `name` must not be empty or blank.
    *   - The `numberOfTrack` must be greater than 0.
    */
  case class CheckedStation[N: Numeric, C <: Coordinate[N]](
      name: String,
      coordinate: C,
      numberOfTrack: Int
  ) extends Station[N, C]

/** Defines a Selectable Object. */
trait Selectable:
  val selected: Boolean

/** Represents a selectable station.
  *
  * @param station
  *   the station instance of type `Station[L]` to be wrapped
  * @param selected
  *   a boolean flag indicating whether the station is selected
  */
final case class SelectableStation[N: Numeric, C <: Coordinate[N]](
    station: Station[N, C],
    selected: Boolean
) extends Station[N, C] with Selectable:
  export station.*
