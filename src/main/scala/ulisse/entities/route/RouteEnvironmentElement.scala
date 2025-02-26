package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Track.TrainAgentsDirection
import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given

trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper[RouteEnvironmentElement]:
  type TAC = Track
  def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[RouteEnvironmentElement]

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(
      route,
      TrainAgentsContainer.generateSequentialContainers(i => Track(i), route.railsCount)
    )

  private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[Track])
      extends RouteEnvironmentElement:
    export route.*

    override def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[RouteEnvironmentElement] =
      (for
        firstAvailableContainer <- containers.find(_.isAvailable)
        updatedContainer <- containers.updateWhenWithEffects(_ == firstAvailableContainer)(_.putTrain(train, direction))
      yield copy(containers = updatedContainer)) when !contains(train) // firstAvailableContainer.isEmpty &&

    override def buildNewEnvironmentElement(containers: Seq[Track]): RouteEnvironmentElement =
      copy(containers = containers)
