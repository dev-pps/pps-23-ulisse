package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.Trains.Train

trait RouteEnvironmentElement extends Route with EnvironmentElement:
  val trains: Seq[Seq[Train]]

object RouteEnvironmentElement:

  extension (train: Train)
    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(_.trains.exists(_.contains(train)))
