package ulisse.entities.station

import ulisse.entities.Coordinates.Coordinate
import ulisse.utils.Errors.AppError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a Station.
  *
  * A Station represents a location with tracks and associated metadata.
  *
  * **Requirements**:
  *   - The `name` must not be empty or blank.
  *   - The `numberOfTrack` must be greater than 0.
  *
  * @tparam L
  *   The type of the location associated with the station.
  */
trait Station[N: Numeric, C <: Coordinate[N]]:
  val name: String
  val location: C
  val numberOfTrack: Int

/** Factory for [[Station]] instances. */
object Station:

  def apply[N: Numeric, C <: Coordinate[N]](
      name: String,
      location: C,
      numberOfTrack: Int
  ): Station[N, C] =
    StationImpl(name, location, numberOfTrack)

  /** Creates a `Station` instance with validation.
    *
    * @param name
    *   The name of the station. Must not be empty or blank.
    * @param location
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks. Must be greater than 0.
    * @return
    *   Either a `Station` instance or an `Errors` indicating the issue.
    */
  def createCheckedStation[N: Numeric, C <: Coordinate[N]](
      name: String,
      location: C,
      numberOfTrack: Int
  ): Either[AppError, CheckedStation[N, C]] =
    for
      validName          <- validateNonBlankString(name, CheckedStation.Error.InvalidName)
      validNumberOfTrack <- validatePositive(numberOfTrack, CheckedStation.Error.InvalidNumberOfTrack)
    yield CheckedStation(validName, location, validNumberOfTrack)

  private final case class StationImpl[N: Numeric, C <: Coordinate[N]](
      name: String,
      location: C,
      numberOfTrack: Int
  ) extends Station[N, C]

  object CheckedStation:
    /** Represents errors that can occur during station creation. */
    enum Error extends AppError:
      case InvalidName, InvalidNumberOfTrack

  /** Defines a CheckedStation.
   *
   * A Station represents a location with tracks and associated metadata.
   *
   * **Requirements**:
   *   - The `name` must not be empty or blank.
   *   - The `numberOfTrack` must be greater than 0.
   *
   * @tparam L
   * The type of the location associated with the station.
   */
  case class CheckedStation[N: Numeric, C <: Coordinate[N]](
      name: String,
      location: C,
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
  * @tparam L
  *   The type of the location associated with the station.
  */
final case class SelectableStation[N: Numeric, C <: Coordinate[N]](
    station: Station[N, C],
    selected: Boolean
) extends Station[N, C] with Selectable:
  export station.*
