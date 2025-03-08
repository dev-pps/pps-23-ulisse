package ulisse.infrastructures.view.map

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook

import scala.swing.{Graphics2D, Panel}

/** Represent the map simulation. */
trait MapSimulation extends Panel with EnhancedLook:
  /** Draw the station on the screen. */
  def uploadStation(newStations: Seq[StationEnvironmentElement]): Unit

  /** Draw the route on the screen. */
  def uploadRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit

/** Companion object for [[MapSimulation]]. */
object MapSimulation:

  /** Create a new [[MapSimulation]]. */
  def apply(): MapSimulation = MapSimulationImpl()

  private case class MapSimulationImpl() extends MapSimulation:
    private val stations = MapElements[StationEnvironmentElement](observable)
    private val routes   = MapElements[RouteEnvironmentElement](observable)

    override def uploadStation(newStations: Seq[StationEnvironmentElement]): Unit =
      stations update (newStations map MapElement.createStationEnvironmentElement)
      updateGraphics()

    override def uploadRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit =
      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      var routeCheck: List[(RouteEnvironmentElement, Boolean)] = List.empty

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
