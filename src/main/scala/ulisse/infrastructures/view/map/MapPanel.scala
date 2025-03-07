package ulisse.infrastructures.view.map

import ulisse.applications.ports.StationPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook

import scala.swing.*

/** Represent the map panel. */
trait MapPanel extends Panel with EnhancedLook:
  /** Attach the station form to the map panel. */
  def attachClickStation(event: ClickObserver[MapElement[Station]]): Unit

  /** Attach the route form to the map panel. */
  def attachClickRoute(event: ClickObserver[MapElement[Route]]): Unit

  /** Draw the station on the screen. */
  def uploadStation(newStations: StationPorts.Input#SM): Unit

  /** Draw the route on the screen. */
  def updateRoutes(newRoutes: List[Route]): Unit

  /** Draw the route on the screen. */
  def updateGraphics(routes: List[Route]): Unit

/** Companion object for [[MapPanel]]. */
object MapPanel:
  /** Create a new [[MapPanel]]. */
  def apply(): MapPanel = MapPanelImpl()

  private case class MapPanelImpl() extends MapPanel:
    private val stations = MapElements[Station](observable)
    private val routes   = MapElements[Route](observable)

    def attachClickStation(event: ClickObserver[MapElement[Station]]): Unit = stations attachClick event

    def attachClickRoute(event: ClickObserver[MapElement[Route]]): Unit = routes attachClick event

    override def uploadStation(newStations: StationPorts.Input#SM): Unit =
      stations update (newStations map (MapElement createStation (_, ImagePath.station)))
      updateGraphics()

    override def updateRoutes(newRoutes: List[Route]): Unit =
      routes update (newRoutes map (MapElement createRoute (_, ImagePath.route)))
      updateGraphics()

    def updateGraphics(newRoutes: List[Route]): Unit =
      routes update (newRoutes map (MapElement createRoute (_, ImagePath.route)))

    override protected def paintLook(g: Graphics2D): Unit =
      stations draw (g, peer)
      routes draw (g, peer)
      super.paintLook(g)
