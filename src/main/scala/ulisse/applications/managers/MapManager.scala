package ulisse.applications.managers

import ulisse.entities.Coordinates.{Coordinate, Geo}
import ulisse.entities.station.Station

trait MapManager extends StationManager[Double, Coordinate[Double], Station[Double, Coordinate[Double]]]
    with RouteManager

object MapManager:
  def apply(): MapManager = MapManagerImpl(StationManager(), RouteManager.empty())

  private case class MapManagerImpl(
      station: StationManager[Double, Coordinate[Double], Station[Double, Coordinate[Double]]],
      route: RouteManager
  ) extends MapManager:
    export station._, route._
