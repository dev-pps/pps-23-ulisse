package ulisse.entities.route

import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.Trains.Train

trait RouteEnvironmentElement extends Route with EnvironmentElement:
  val trains: Seq[Seq[Train]]

object RouteEnvironmentElement:

  extension (train: Train)
    def take(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      route.firstAvailableTrack.map(track => route.updateTrack(track, Some(train)))
      station.firstAvailableTrack.map(track => station.updateTrack(track, Some(train)))

    def leave(route: RouteEnvironmentElement): Option[RouteEnvironmentElement] =
      station.tracks.find(_.train.contains(train)).map(track => station.updateTrack(track, None))
      
    def findInRoutes(routes: Seq[RouteEnvironmentElement]): Option[RouteEnvironmentElement] =
      routes.find(_.trains.exists(_.contains(train)))
