package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.train.Trains.Train

trait RouteEnvironmentElement extends Route:
  val trains: Seq[Seq[Train]]
