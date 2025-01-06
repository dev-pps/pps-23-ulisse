import cats.implicits.catsSyntaxEq

final case class StationMap(stations: List[Station]):
  require(
    stations.map(_.name).distinct.size === stations.size,
    "station names must be unique"
  )
  require(
    stations.map(_.location).distinct.size === stations.size,
    "station locations must be unique"
  )
