package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Track.existInTrack
import ulisse.entities.simulation.Environments
import ulisse.entities.simulation.Environments.TrainAgentEEWrapper
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given_Conversion_Option_Option

trait RouteEnvironmentElement extends Route with TrainAgentEEWrapper[RouteEnvironmentElement]:
  override type TAC = Track
  val tracks: Seq[TAC]
  def firstAvailableTrack: Option[TAC] = tracks.find(_.isAvailable)

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Track()))

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.flatMap(track => route.putTrain(track, train))

    // TODO add testing (is equal to removeTrain)
    def leave(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.removeTrain(train)

    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(train.existInRoute)

    def existInRoute(route: RouteEnvironmentElement): Boolean =
      route.tracks.exists(train.existInTrack)

  private final case class RouteEnvironmentElementImpl(route: Route, tracks: Seq[Track])
      extends RouteEnvironmentElement:
    export route.*

    private def whenTrainExists[A](f: => A)(using train: TrainAgent): Option[A] =
      f when train.existInRoute(this)

    private def modifyItInTrack(f: TAC => TAC)(using
        train: TrainAgent
    ): RouteEnvironmentElementImpl =
      copy(tracks = tracks.updateWhen(train.existInTrack)(f))

    def putTrain(track: TAC, train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateFirstWhenWithEffects(_ == track)(_.putTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when track.isAvailable && !(train existInRoute this)

    def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      // TODO investigate why is not working using whenTrainExists
      tracks.updateWhenWithEffects(train.existInTrack)(_.updateWhen(train.matchId)(_ => train)).toOption.map(tracks =>
        copy(tracks = tracks)
      ) when (train existInRoute this)

    def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      println(train existInRoute this)
      tracks.updateWhenWithEffects(train.existInTrack)(_.removeTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when (train existInRoute this)
