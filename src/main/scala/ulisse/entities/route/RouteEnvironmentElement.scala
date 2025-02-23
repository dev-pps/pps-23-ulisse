package ulisse.entities.route

import ulisse.entities
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given_Conversion_Option_Option
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.simulation.Simulations.Actions.SimulationAction
import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.entities.route.Track.existInTrack

trait RouteEnvironmentElement extends Route with EnvironmentElement[RouteEnvironmentElement]:
  override type TrainContainer = Track
  val tracks: Seq[TrainContainer]
  def firstAvailableTrack: Option[TrainContainer] = tracks.find(_.isAvailable)

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

    private def modifyItInTrack(f: Track => Track)(using
        train: TrainAgent
    ): RouteEnvironmentElementImpl =
      copy(tracks = tracks.updateWhen(train.existInTrack)(f))

    def putTrain(track: Track, train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateFirstWhenWithEffects(_ == track)(_.putTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when track.isAvailable && !(train existInRoute this)

    def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      // TODO investigate why is not working using whenTrainExists
      tracks.updateWhenWithEffects(train.existInTrack)(_.updateWhen(train.matchId)(_ => train)).toOption.map(tracks =>
        copy(tracks = tracks)
      ) when (train existInRoute this)

    def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateWhenWithEffects(train.existInTrack)(_.removeTrain(train)).map(tracks =>
        copy(tracks = tracks)
      ) when (train existInRoute this)
