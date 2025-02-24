package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given

trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper[RouteEnvironmentElement]

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(
      route,
      TrainAgentsContainer.generateSequentialContainers(i => Track(i), route.railsCount)
    )

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableContainer.flatMap(track => route.putTrain(track, train))

  private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[TrainAgentsContainer])
      extends RouteEnvironmentElement:
    export route.*

    def putTrain(track: TrainAgentsContainer, train: TrainAgent): Option[RouteEnvironmentElement] =
      containers.updateFirstWhenWithEffects(_ == track)(_.putTrain(train)).map(tracks =>
        copy(containers = tracks)
      ) when track.isAvailable && !contains(train)

    override def buildNewEnvironmentElement(containers: Seq[TrainAgentsContainer]): RouteEnvironmentElement =
      copy(containers = containers)
