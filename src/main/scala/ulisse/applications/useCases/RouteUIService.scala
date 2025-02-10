package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.managers.RouteManager
import ulisse.applications.managers.RouteManager.ErrorSaving
import ulisse.applications.ports.RoutePorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object RouteUIService:

  object RouteUIInputService:
    def apply[N: Numeric, C <: Coordinate[N]](queue: LinkedBlockingQueue[RouteManager[N, C] => RouteManager[N, C]])
        : RoutePorts.UIInputPort[N, C] = RouteUIInputServiceImpl(queue)

    private case class RouteUIInputServiceImpl[N: Numeric, C <: Coordinate[N]](
        queue: LinkedBlockingQueue[RouteManager[N, C] => RouteManager[N, C]]
    ) extends RoutePorts.UIInputPort[N, C]:

      override def save(optRoute: Option[Route[N, C]]): Future[Either[RouteManager.ErrorSaving, List[Route[N, C]]]] =
        val promise = Promise[Either[RouteManager.ErrorSaving, List[Route[N, C]]]]()
        queue.offer(manager => {
          val either: Either[ErrorSaving, RouteManager[N, C]] =
            optRoute.map(route => manager.save(route)).getOrElse(Left(ErrorSaving.creation))
          either match
            case Left(error) =>
              promise.success(error.asLeft)
              manager
            case Right(newManager) =>
              promise.success(newManager.routes.asRight)
              newManager
        })
        promise.future
