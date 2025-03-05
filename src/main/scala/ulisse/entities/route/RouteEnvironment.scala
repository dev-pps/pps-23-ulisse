package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.Station
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen

trait RouteEnvironment:
  def routes: Seq[RouteEnvironmentElement]
  def findRoutesWithTravelDirection(route: (Station, Station)): Seq[(RouteEnvironmentElement, TrackDirection)]
  def putTrain(train: TrainAgent, route: (Station, Station)): Option[RouteEnvironment]
  def updateTrain(train: TrainAgent): Option[RouteEnvironment]
  def removeTrain(train: TrainAgent): Option[RouteEnvironment]

object RouteEnvironment:
  def apply(configurationData: ConfigurationData): RouteEnvironment = RouteEnvironmentImpl(configurationData.routes)

  private final case class RouteEnvironmentImpl(routes: Seq[RouteEnvironmentElement]) extends RouteEnvironment:

    private def doOperationOnRoute(
        train: TrainAgent,
        operation: RouteEnvironmentElement => Option[RouteEnvironmentElement]
    ): Option[RouteEnvironment] =
      for
        route        <- routes.find(_.contains(train))
        updatedRoute <- operation(route)
      yield copy(routes = routes.updateWhen(_ == route)(_ => updatedRoute))

    private def findRouteDirection(route: Route, routeStations: (Station, Station)): TrackDirection =
      if route.isRightDirection.tupled(routeStations) then TrackDirection.Forward else TrackDirection.Backward

    def findRoutesWithTravelDirection(route: (Station, Station)): Seq[(RouteEnvironmentElement, TrackDirection)] =
      routes.filter(_.isPath.tupled(route)).map(r => (r, findRouteDirection(r, route)))

    private def updatedAvailableEnvironment(
        train: TrainAgent,
        route: (Station, Station)
    ): Seq[RouteEnvironmentElement] =
      for
        routeAndDirection <- findRoutesWithTravelDirection(route)
        updatedRoute      <- routeAndDirection._1.putTrain(train.resetDistanceTravelled(), routeAndDirection._2)
      yield updatedRoute

    override def putTrain(train: TrainAgent, route: (Station, Station)): Option[RouteEnvironment] =
      updatedAvailableEnvironment(train, route)
        .headOption
        .map(updatedRoutes => copy(routes = routes.updateWhen(_.id == updatedRoutes.id)(_ => updatedRoutes)))

    override def updateTrain(train: TrainAgent): Option[RouteEnvironment] =
      doOperationOnRoute(train, _.updateTrain(train))

    override def removeTrain(train: TrainAgent): Option[RouteEnvironment] =
      doOperationOnRoute(train, _.removeTrain(train))
