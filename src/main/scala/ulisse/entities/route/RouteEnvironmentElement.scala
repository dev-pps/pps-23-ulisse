package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.when

trait RouteEnvironmentElement extends Route with EnvironmentElement:
  val tracks: Seq[Track]
  def firstAvailableTrack: Option[Track] = tracks.find(_.isAvailable)
  def putTrain(track: Track, train: TrainAgent): Option[RouteEnvironmentElement]
  def updateTrain(train: TrainAgent): Option[RouteEnvironmentElement]
  def removeTrain(train: TrainAgent): Option[RouteEnvironmentElement]

object RouteEnvironmentElement:

  // TODO evaluate moving this in factory method
  val minPermittedDistanceBetweenTrains: Double = 100.0

  def apply(route: Route): RouteEnvironmentElement =
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Track(minPermittedDistanceBetweenTrains)))

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.flatMap(track => route.putTrain(track, train))

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

    private def whenTrainExists[A](f: => A)(using train: Train): Option[A] =
      f when train.existInRoute(tracks)

    private def modifyItInTrack(f: Track => Track)(using
        train: TrainAgent
    ): RouteEnvironmentElementImpl =
      copy(tracks = tracks.updateWhen(train.existInTrack)(f))

    def putTrain(track: Track, train: TrainAgent): Option[RouteEnvironmentElement] =
      Some(copy(tracks = tracks))
//        tracks.updateFirstWhen(_ == track)(_ :+ train)
//      ) when track.isAvailable && !train.existInRoute(tracks)

    def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      Some(this)
//      whenTrainExists:
//        modifyItInTrack(_.updateWhen(train.matchId)(_ => train))

    def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      whenTrainExists:
        modifyItInTrack(_.filterNot(train.matchId))
