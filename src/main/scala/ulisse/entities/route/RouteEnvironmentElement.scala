package ulisse.entities.route

import ulisse.entities
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*

trait RouteEnvironmentElement extends Route with EnvironmentElement:
  val minPermittedDistance: Double
  val trains: Seq[Seq[TrainAgent]]
  def isTrackAvailable(track: Seq[TrainAgent]): Boolean =
    track.forall(t => t.distanceTravelled - t.length >= minPermittedDistance)
  def firstAvailableTrack: Option[Seq[TrainAgent]] = trains.find(isTrackAvailable)
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
      route.trains.find(_.contains(train)).flatMap(track => route.removeTrain(train))

    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(_.trains.exists(_.contains(train)))

  private final case class RouteEnvironmentElementImpl(route: Route, trains: Seq[Seq[TrainAgent]])
      extends RouteEnvironmentElement:
    export route.*
    val minPermittedDistance: Double = 100.0
    def putTrain(routeTrack: Seq[TrainAgent], train: TrainAgent): Option[RouteEnvironmentElement] =
      if isTrackAvailable(routeTrack) && !trains.exists(_.exists(_.name == train.name)) then
        Some(copy(trains = trains.updateFirstWhen(_ == routeTrack)(_ ++ Seq(train))))
      else None
    def updateTrain(train: TrainAgent): Option[RouteEnvironmentElement] =
      if trains.exists(_.exists(_.name == train.name)) then
        Some(copy(trains =
          trains.updateWhen(_.exists(_.name == train.name))(_.updateWhen(_.name == train.name)(_ => train))
        ))
      else None
    def removeTrain(train: TrainAgent): Option[RouteEnvironmentElement] =
      if trains.exists(_.exists(_.name == train.name)) then
        Some(copy(trains = trains.updateWhen(_.exists(_.name == train.name))(_.filterNot(_.name == train.name))))
      else None
