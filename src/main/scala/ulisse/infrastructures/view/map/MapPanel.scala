package ulisse.infrastructures.view.map

import ulisse.applications.ports.StationPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
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
  def updateStation(newStations: StationPorts.Input#SM): Unit

  /** Draw the route on the screen. */
  def updateRoutes(newRoutes: List[Route]): Unit

/** Companion object for [[MapPanel]]. */
object MapPanel:
  /** Create a new [[MapPanel]]. */
  def apply(): MapPanel = MapPanelImpl()

  private case class MapPanelImpl() extends MapPanel:
    private val stations = MapElements[Station](observable)
    private val routes   = MapElements[Route](observable)

    override def attachClickStation(event: ClickObserver[MapElement[Station]]): Unit = stations attachClick event

    override def attachClickRoute(event: ClickObserver[MapElement[Route]]): Unit = routes attachClick event

    override def updateStation(newStations: StationPorts.Input#SM): Unit =
      stations update (newStations map MapElement.createStation)
      updateGraphics()

    override def updateRoutes(newRoutes: List[Route]): Unit =
      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      var routeCheck: List[(Route, Boolean)] = List.empty

      newRoutes.foreach(route =>
        if routeCheck.exists((a, b) => a.isPath(route.departure, route.arrival) && !b) then
          routeCheck = routeCheck.::(route, true)
        else
          routeCheck = routeCheck.::(route, false)
      )

      routes update (routeCheck map MapElement.createRoute)
      updateGraphics()

    override protected def paintLook(g: Graphics2D): Unit =
      routes draw (g, peer)
      stations draw (g, peer)
      super.paintLook(g)
