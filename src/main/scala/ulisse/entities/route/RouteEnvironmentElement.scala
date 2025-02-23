package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.EnvironmentElements.TrainAgentEEWrapper
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given

trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper[RouteEnvironmentElement]:
  override type TAC = Track
  val tracks: Seq[TAC]
  def firstAvailableTrack: Option[TAC]              = tracks.find(_.isAvailable)
  override def contains(train: TrainAgent): Boolean = tracks.exists(_.contains(train))

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Track()))

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.flatMap(track => route.putTrain(track, train))

  private final case class RouteEnvironmentElementImpl(route: Route, tracks: Seq[Track])
      extends RouteEnvironmentElement:
    export route.*

    def putTrain(track: TAC, train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateFirstWhenWithEffects(_ == track)(_.putTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when track.isAvailable && !contains(train)

    def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      // TODO investigate why is not working using whenTrainExists
      tracks.updateWhenWithEffects(_.contains(train))(_.updateTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when contains(train)

    def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateWhenWithEffects(_.contains(train))(_.removeTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when contains(train)
