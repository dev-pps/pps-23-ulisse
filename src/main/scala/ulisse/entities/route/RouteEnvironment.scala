package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.Environments.TrainAgentEnvironment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.Station
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen

trait RouteEnvironment extends TrainAgentEnvironment[RouteEnvironment, RouteEnvironmentElement]:
  def findRoutesWithTravelDirection(route: (Station, Station)): Seq[(RouteEnvironmentElement, TrackDirection)]
  def putTrain(train: TrainAgent, route: (Station, Station)): Option[RouteEnvironment]

object RouteEnvironment:
  def apply(configurationData: ConfigurationData): RouteEnvironment = RouteEnvironmentImpl(configurationData.routes)

  private final case class RouteEnvironmentImpl(environmentElements: Seq[RouteEnvironmentElement])
      extends RouteEnvironment:
    override protected def constructor(environmentElements: Seq[RouteEnvironmentElement]): RouteEnvironment =
      copy(environmentElements)

    private def findRouteDirection(route: Route, routeStations: (Station, Station)): TrackDirection =
      if route.isRightDirection.tupled(routeStations) then TrackDirection.Forward else TrackDirection.Backward

    override def findRoutesWithTravelDirection(route: (Station, Station))
        : Seq[(RouteEnvironmentElement, TrackDirection)] =
      environmentElements.filter(_.isPath.tupled(route)).map(r => (r, findRouteDirection(r, route)))

    private def updatedAvailableEnvironment(
        train: TrainAgent,
        route: (Station, Station)
    ): Seq[RouteEnvironmentElement] =
      for
        (r, d)       <- findRoutesWithTravelDirection(route)
        updatedRoute <- r.putTrain(train.resetDistanceTravelled, d)
      yield updatedRoute

    override def putTrain(train: TrainAgent, route: (Station, Station)): Option[RouteEnvironment] =
      updatedAvailableEnvironment(train, route).headOption
        .map(updatedRoutes => constructor(environmentElements.updateWhen(_.id == updatedRoutes.id)(_ => updatedRoutes)))
