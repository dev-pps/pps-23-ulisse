package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtilities.when

trait RouteEnvironmentElement extends Route with EnvironmentElement:
  val minPermittedDistanceBetweenTrains: Double
  val tracks: Seq[Seq[TrainAgent]]
  extension (track: Seq[TrainAgent])
    def isAvailable: Boolean = track.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
  def firstAvailableTrack: Option[Seq[TrainAgent]] = tracks.find(_.isAvailable)
  def putTrain(routeTrack: Seq[TrainAgent], train: TrainAgent): Option[RouteEnvironmentElement]
  def updateTrain(train: TrainAgent): Option[RouteEnvironmentElement]
  def removeTrain(train: TrainAgent): Option[RouteEnvironmentElement]

object RouteEnvironmentElement:

  def createRouteEnvironmentElement(route: Route): RouteEnvironmentElement =
    RouteEnvironmentElementImpl(route, Seq.fill(route.railsCount)(Seq()))

  extension (train: TrainAgent)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.flatMap(track => route.putTrain(track, train))

    def leave(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.tracks.find(_.contains(train)).flatMap(track => route.removeTrain(train))

    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(_.tracks.exists(_.contains(train)))

  private final case class RouteEnvironmentElementImpl(route: Route, tracks: Seq[Seq[TrainAgent]])
      extends RouteEnvironmentElement:
    export route.*
    // TODO evaluate moving this in factory method
    val minPermittedDistanceBetweenTrains: Double = 100.0
    extension (train: Train)
      private def existInRoute(trains: Seq[Seq[TrainAgent]]): Boolean = trains.exists(train.existInTrack)
      private def existInTrack(trains: Seq[TrainAgent]): Boolean      = trains.exists(train.matchId)
      private def matchId(otherTrain: Train): Boolean                 = train.name == otherTrain.name

    private def whenTrainExists[A](f: => A)(using train: Train): Option[A] =
      f when train.existInRoute(tracks)

    private def modifyItInTrack(f: Seq[TrainAgent] => Seq[TrainAgent])(using
        train: Train
    ): RouteEnvironmentElementImpl =
      copy(tracks = tracks.updateWhen(train.existInTrack)(f))

    def putTrain(routeTrack: Seq[TrainAgent], train: TrainAgent): Option[RouteEnvironmentElement] =
      copy(tracks =
        tracks.updateFirstWhen(_ == routeTrack)(_ ++ Seq(train))
      ) when routeTrack.isAvailable && !train.existInRoute(tracks)
    def updateTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      whenTrainExists:
        modifyItInTrack(_.updateWhen(train.matchId)(_ => train))
    def removeTrain(using train: TrainAgent): Option[RouteEnvironmentElement] =
      whenTrainExists:
        modifyItInTrack(_.filterNot(train.matchId))
