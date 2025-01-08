package model.station

import cats.Eq

/** Represents a geographic location with latitude and longitude. */
object Location:

  /** Opaque type for a location represented by latitude and longitude. */
  opaque type Location = (Double, Double)

  /** Implicit equality comparison for `Location` values. */
  given Eq[Location] = Eq.fromUniversalEquals

  /** Creates a `Location` ensuring valid latitude and longitude ranges.
    *
    * @param latitude
    *   The latitude of the location. Must be in the range [-90, 90].
    * @param longitude
    *   The longitude of the location. Must be in the range [-180, 180].
    * @throws IllegalArgumentException
    *   If the latitude or longitude is out of range.
    */
  def apply(latitude: Double, longitude: Double): Location =
    require(
      latitude >= -90.0 && latitude <= 90.0,
      s"Invalid latitude: $latitude"
    )
    require(
      longitude >= -180.0 && longitude <= 180.0,
      s"Invalid longitude: $longitude"
    )
    (latitude, longitude)

  /** Extension methods for accessing the latitude and longitude of a
    * `Location`.
    */
  extension (location: Location)
    def latitude: Double  = location._1
    def longitude: Double = location._2
