package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.{Track, TrackDirection}
import ulisse.entities.simulation.environments.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.environments.Environment
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given

/** Defines a route for simulation. */
trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper[RouteEnvironmentElement]:
  /** Type of the TrainAgentsContainer */
  type TAC = Track

  /** Try to put train inside a track given the desired direction */
  def putTrain(train: TrainAgent, direction: TrackDirection): Option[RouteEnvironmentElement]

  /** Check if the route is available for a train to be put in */
  def isAvailable(direction: TrackDirection): Boolean = containers.exists(_.isAvailable(direction))

  /** Defines equality for RouteEnvironmentElement */
  override def equals(that: Any): Boolean =
    that match
      case r: RouteEnvironmentElement =>
        containers == r.containers &&
        this === r
      case r: Route =>
        this === r

/** Factory for [[RouteEnvironmentElement]] instances. */
object RouteEnvironmentElement:

  /** Creates a `RouteEnvironmentElement` instance with sequential containers. */
  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(
      route,
      TrainAgentsContainer.generateSequentialContainers(i => Track(i), route.railsCount)
    )

  private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[Track])
      extends RouteEnvironmentElement:
    export route.*

    override def putTrain(train: TrainAgent, direction: TrackDirection): Option[RouteEnvironmentElement] =
      (for
        firstAvailableContainer <- containers.find(_.isAvailable(direction))
        updatedContainers <-
          containers.updateWhenWithEffects(_ == firstAvailableContainer)(_.putTrain(train, direction))
      yield updateEEContainers(updatedContainers)) when !contains(train)

    override def updateEEContainers(containers: Seq[Track]): RouteEnvironmentElement =
      copy(containers = containers)
