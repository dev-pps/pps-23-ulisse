package model.station

import cats.Eq

object Location:
  opaque type Location = (Double, Double)
  given Eq[Location] = Eq.fromUniversalEquals
  def apply(latitude: Double, longitude: Double): Location =
    require(
      latitude >= -90.0 && latitude <= 90.0,
      s"invalid latitude: $latitude"
    )
    require(
      longitude >= -180.0 && longitude <= 180.0,
      s"invalid longitude: $longitude"
    )
    (latitude, longitude)
  extension (location: Location)
    def latitude: Double  = location._1
    def longitude: Double = location._2
