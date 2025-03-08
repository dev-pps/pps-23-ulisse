package ulisse.infrastructures.view.map

import ulisse.entities.route.Routes.Route
import ulisse.entities.route.{RouteEnvironmentElement, Tracks}
import ulisse.entities.station.StationEnvironmentElement
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.utils.Swings.{computePosition, given_ExecutionContext, *}

import java.awt.geom.Point2D
import scala.swing.{Graphics2D, Panel}

/** Represent the map simulation. */
trait MapSimulation extends Panel with EnhancedLook:
  /** Attach the station form to the map panel. */
  def attachClickStation(event: ClickObserver[MapElement[StationEnvironmentElement]]): Unit

  /** Attach the route form to the map panel. */
  def attachClickRoute(event: ClickObserver[MapElement[RouteEnvironmentElement]]): Unit

  /** Draw the station on the screen. */
  def uploadStation(newStations: Seq[StationEnvironmentElement]): Unit

  /** Draw the route on the screen. */
  def uploadRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit

  /** Draw the train on the screen. */
  def uploadTrain(newRoutes: Seq[RouteEnvironmentElement]): Unit

/** Companion object for [[MapSimulation]]. */
object MapSimulation:

  /** Create a new [[MapSimulation]]. */
  def apply(): MapSimulation = MapSimulationImpl()

  private case class MapSimulationImpl() extends MapSimulation:
    private val stations = MapElements[StationEnvironmentElement](observable)
    private val routes   = MapElements[RouteEnvironmentElement](observable)
    private val trains   = MapElements[Point2D.Double](observable)

    override def attachClickStation(event: ClickObserver[MapElement[StationEnvironmentElement]]): Unit =
      stations attachClick event

    override def attachClickRoute(event: ClickObserver[MapElement[RouteEnvironmentElement]]): Unit =
      routes attachClick event

    override def uploadStation(newStations: Seq[StationEnvironmentElement]): Unit =
      stations update (newStations map MapElement.createStationEnvironmentElement)
      updateGraphics()

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    override def uploadRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit =
      var routeCheck: List[(RouteEnvironmentElement, Boolean)] = List.empty
      newRoutes.foreach(route =>
        if routeCheck.exists((a, b) => a.isPath(route.departure, route.arrival) && !b) then
          routeCheck = routeCheck.::(route, true)
        else routeCheck = routeCheck.::(route, false)
      )

      routes update (routeCheck map MapElement.createRoute)
      updateGraphics()

    override def uploadTrain(newRoutes: Seq[RouteEnvironmentElement]): Unit =
      newRoutes.foreach(route =>
        val env                 = route.containers
        val departureCoordinate = route.departure.coordinate
        val arrivalCoordinate   = route.arrival.coordinate
        env.foreach(rails =>
          val trainsEnv = rails.trains
          val positions = trainsEnv.map(train =>
            rails.currentDirection map {
              case Tracks.TrackDirection.Forward =>
                departureCoordinate.toPoint2D computePosition (arrivalCoordinate.toPoint2D, train.distanceTravelled)
              case Tracks.TrackDirection.Backward =>
                arrivalCoordinate.toPoint2D computePosition (departureCoordinate.toPoint2D, train.distanceTravelled)
            }
          )
          val checkPosition = positions.foldLeft(List[Point2D.Double]())((acc, pos) => acc ++ pos)
          trains update (checkPosition map MapElement.createTrain)
        )
      )
      updateGraphics()

    override protected def paintLook(g: Graphics2D): Unit =
      routes draw (g, peer)
      stations draw (g, peer)
      super.paintLook(g)
