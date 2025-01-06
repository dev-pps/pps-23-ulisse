object Location:
  opaque type Location = (Double, Double)
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
