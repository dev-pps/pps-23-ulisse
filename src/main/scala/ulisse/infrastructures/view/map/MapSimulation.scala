package ulisse.infrastructures.view.map

import ulisse.entities.route.{RouteEnvironmentElement, Tracks}
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.TrainAgents
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.map.MapSimulation.TrainMapElement
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.geom.Point2D
import scala.swing.{Graphics2D, Panel}

/** Represent the map simulation. */
trait MapSimulation extends Panel with EnhancedLook:
  /** Attach the station form to the map panel. */
  def attachClickStation(event: ClickObserver[MapElement[StationEnvironmentElement]]): Unit

  /** Attach the route form to the map panel. */
  def attachClickRoute(event: ClickObserver[MapElement[RouteEnvironmentElement]]): Unit

  /** Attach the train form to the map panel. */
  def attachClickTrain(event: ClickObserver[MapElement[TrainMapElement]]): Unit

  /** Draw the station on the screen. */
  def updateStation(newStations: Seq[StationEnvironmentElement]): Unit

  /** Draw the route on the screen. */
  def updateRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit

  /** Draw the train on the screen. */
  def updateTrain(newRoutes: Seq[RouteEnvironmentElement]): Unit

/** Companion object for [[MapSimulation]]. */
object MapSimulation:

  /** Create a new [[MapSimulation]]. */
  def apply(): MapSimulation = MapSimulationImpl()

  /** Represents the map simulation implementation. */
  final case class TrainMapElement(train: TrainAgents.TrainAgent, position: Point2D.Double)

  private case class MapSimulationImpl() extends MapSimulation:
    private val stations = MapElements[StationEnvironmentElement](observable)
    private val routes   = MapElements[RouteEnvironmentElement](observable)
    private val trains   = MapElements[TrainMapElement](observable)

    override def attachClickStation(event: ClickObserver[MapElement[StationEnvironmentElement]]): Unit =
      stations attachClick event

    override def attachClickRoute(event: ClickObserver[MapElement[RouteEnvironmentElement]]): Unit =
      routes attachClick event

    override def attachClickTrain(event: ClickObserver[MapElement[TrainMapElement]]): Unit =
      trains attachClick event

    override def updateStation(newStations: Seq[StationEnvironmentElement]): Unit =
      stations update (newStations map MapElement.createStationEnvironmentElement)
      updateGraphics()

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    override def updateRoutes(newRoutes: Seq[RouteEnvironmentElement]): Unit =
      var routeCheck: List[(RouteEnvironmentElement, Boolean)] = List.empty
      newRoutes.foreach(route =>
        if routeCheck.exists((a, b) => a.isPath(route.departure, route.arrival) && !b) then
          routeCheck = routeCheck.::(route, true)
        else routeCheck = routeCheck.::(route, false)
      )

      routes update (routeCheck map MapElement.createRoute)
      updateGraphics()

    override def updateTrain(newRoutes: Seq[RouteEnvironmentElement]): Unit =
      val trainsWithPosition = newRoutes.flatten(route =>
        val env                 = route.containers
        val departureCoordinate = route.departure.coordinate
        val arrivalCoordinate   = route.arrival.coordinate
        val stationDistance     = departureCoordinate.toPoint2D distance arrivalCoordinate.toPoint2D
        env.flatten(rails =>
          val trainsEnv = rails.trains
          trainsEnv.map(train =>
            val normalizedTrainDistance = train.distanceTravelled * stationDistance / route.length
            val pos = rails.currentDirection map {
              case Tracks.TrackDirection.Forward =>
                departureCoordinate.toPoint2D computePosition (arrivalCoordinate.toPoint2D, normalizedTrainDistance)
              case Tracks.TrackDirection.Backward =>
                arrivalCoordinate.toPoint2D computePosition (departureCoordinate.toPoint2D, normalizedTrainDistance)
            }
            pos.map((_, train))
          )
        )
      )

      val checkPosition =
        trainsWithPosition.foldLeft(List[TrainMapElement]())((acc, value) =>
          value.fold(acc)((pos, train) =>
            TrainMapElement(train, pos) :: acc
          )
        )
      trains update (checkPosition map MapElement.createTrain)
      updateGraphics()

    override protected def paintLook(g: Graphics2D): Unit =
      routes draw (g, peer)
      stations draw (g, peer)
      trains draw (g, peer)
      super.paintLook(g)
