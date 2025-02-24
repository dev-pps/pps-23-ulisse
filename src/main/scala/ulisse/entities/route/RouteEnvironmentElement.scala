package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given_Conversion_Option_Option

trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper:
  override def trains: Seq[TrainAgent]                  = containers.flatMap(_.trains)
  def firstAvailableTrack: Option[TrainAgentsContainer] = containers.find(_.isAvailable)
  override def contains(train: TrainAgent): Boolean     = containers.exists(_.contains(train))

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Track()))

//  extension (train: TrainAgent)
//    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
//      route.firstAvailableTrack.flatMap(track => route.putTrain(track, train))

  private final case class RouteEnvironmentElementImpl(route: Route, containers: Seq[TrainAgentsContainer])
      extends RouteEnvironmentElement:
    export route.*

    override def containersIDs: Seq[Int] = containers.map(_.id)
    override def putTrain(track: TrainAgentsContainer, train: TrainAgent): Option[RouteEnvironmentElement] =
      containers.updateFirstWhenWithEffects(_ == track)(_.putTrain(train)).map(tracks =>
        copy(containers = tracks)
      ) when track.isAvailable && !contains(train)

    override def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      // TODO investigate why is not working using whenTrainExists
      containers.updateWhenWithEffects(_.contains(train))(_.updateTrain(train)).map(tracks =>
        copy(containers = tracks)
      ) when contains(train)

    override def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      containers.updateWhenWithEffects(_.contains(train))(_.removeTrain(train)).map(tracks =>
        copy(containers = tracks)
      ) when contains(train)
