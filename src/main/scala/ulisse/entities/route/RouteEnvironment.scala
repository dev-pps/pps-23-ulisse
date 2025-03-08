package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.simulation.environments.Environments.TrainAgentEnvironment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.Station
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen

/** Environment that contains Routes for the Simulation. */
trait RouteEnvironment extends TrainAgentEnvironment[RouteEnvironment, RouteEnvironmentElement]:
  /** Find the routes that connect two stations with the travel direction. */
  def findRoutesWithTravelDirection(route: (Station, Station)): Seq[(RouteEnvironmentElement, TrackDirection)]

  /** Try to put a train in a route. */
  def putTrain(train: TrainAgent, route: (Station, Station)): Option[RouteEnvironment]

/** Factory for [[RouteEnvironment]] instances. */
object RouteEnvironment:
  /** Create a new RouteEnvironment from a configurationData. */
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
