package ulisse.applications.useCases

import ulisse.applications.station.StationMap
import ulisse.entities.Coordinates.{Coordinate, Geo}
import ulisse.entities.station.Station

trait MapManager

object MapManager:
  def apply(): MapManager = MapManagerImpl()

  private case class MapManagerImpl() extends MapManager
