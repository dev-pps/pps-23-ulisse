package ulisse.entities.station

import ulisse.entities.Location

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
trait Station[L <: Location]:
  val name: String
  val location: L
  val numberOfTrack: Int

/** Factory for [[Station]] instances. */
object Station:

  /** Represents errors that can occur during station creation. */
  enum Error:
    case InvalidName, InvalidNumberOfTrack

  private def validateName(value: String, error: Error): Either[Error, String] =
    Either.cond(!value.isBlank, value, error)
  private def validateNumberOfTrack(
      value: Int,
      error: Error
  ): Either[Error, Int] =
    Either.cond(value > 0, value, error)

  /** Creates a `Station` instance with validation.
    *
    * @param name
    *   The name of the station. Must not be empty or blank.
    * @param location
    *   The location of the station.
    * @param numberOfTrack
    *   The number of tracks. Must be greater than 0.
    * @return
    *   Either a `Station` instance or an `Error` indicating the issue.
    */
  def apply[L <: Location](
      name: String,
      location: L,
      numberOfTrack: Int
  ): Either[Error, Station[L]] =
    for
      validName <- validateName(name, Error.InvalidName)
      validNumberOfTrack <-
        validateNumberOfTrack(numberOfTrack, Error.InvalidNumberOfTrack)
    yield StationImpl(validName, location, validNumberOfTrack)

  private final case class StationImpl[L <: Location](
      name: String,
      location: L,
      numberOfTrack: Int
  ) extends Station[L]

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
final case class SelectableStation[L <: Location](
    station: Station[L],
    selected: Boolean
) extends Station[L] with Selectable:
  export station.*
