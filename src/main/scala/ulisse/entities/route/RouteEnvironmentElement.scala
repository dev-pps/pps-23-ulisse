package ulisse.entities.route

import ulisse.entities
import ulisse.utils.OptionUtils.*
import ulisse.utils.OptionUtils.given_Conversion_Option_Option
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.{EnvironmentElement, EnvironmentElementContainer}
import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*

trait RouteEnvironmentElement extends Route with EnvironmentElement[RouteEnvironmentElement]:
  val tracks: Seq[Track]
  def firstAvailableTrack: Option[Track] = tracks.find(_.isAvailable)

object RouteEnvironmentElement:

  def apply(route: Route, minPermittedDistanceBetweenTrains: Double): RouteEnvironmentElement =
    given Double = minPermittedDistanceBetweenTrains
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Track()))

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.flatMap(track => route.putAgent(track, train))

    // TODO add testing (is equal to removeTrain)
    def leave(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.removeAgent(train)

    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(ree => train.existInRoute(ree.tracks))

  // TODO evaluate moving in respective classes
  extension (train: Train)
    private[RouteEnvironmentElement] def existInRoute(tracks: Seq[Track]): Boolean =
      tracks.exists(train.existInTrack)
    private[RouteEnvironmentElement] def existInTrack(track: Track): Boolean = track.exists(train.matchId)
    private[RouteEnvironmentElement] def matchId(otherTrain: Train): Boolean = train.name == otherTrain.name

  private final case class RouteEnvironmentElementImpl(route: Route, tracks: Seq[Track])
      extends RouteEnvironmentElement:
    export route.*

    private def whenTrainExists[A](f: => A)(using train: TrainAgent): Option[A] =
      f when train.existInRoute(tracks)

    private def modifyItInTrack(f: Track => Track)(using
        train: TrainAgent
    ): RouteEnvironmentElementImpl =
      copy(tracks = tracks.updateWhen(train.existInTrack)(f))

    override def removeAgent(agent: SimulationAgent): Option[RouteEnvironmentElement] =
      agent match
        case train: TrainAgent =>
          removeTrain(using train)
        case _ => None

    override def putAgent(
        container: EnvironmentElementContainer,
        agent: SimulationAgent
    ): Option[RouteEnvironmentElement] =
      (container, agent) match
        case (track: Track, train: TrainAgent) => putTrain(track, train)
        case _                                 => None

    private def putTrain(track: Track, train: TrainAgent): Option[RouteEnvironmentElement] =
      tracks.updateFirstWhenWithEffects(_ == track)(_ :+ train).toOption.map(tracks =>
        copy(tracks = tracks)
      ) when track.isAvailable && !train.existInRoute(tracks)

    def updateAgent(agent: SimulationAgent): Option[RouteEnvironmentElement] =
      agent match
        case train: TrainAgent => updateTrain(using train)
        case _                 => None

    private def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      // TODO investigate why is not working using whenTrainExists
      tracks.updateWhenWithEffects(train.existInTrack)(_.updateWhen(train.matchId)(_ => train)).toOption.map(tracks =>
        copy(tracks = tracks)
      ) when train.existInRoute(tracks)

    private def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      whenTrainExists:
        modifyItInTrack(_.filterNot(train.matchId))
