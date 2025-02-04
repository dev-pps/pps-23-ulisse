package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.managers.RouteManager
import ulisse.applications.managers.RouteManager.ErrorSaving
import ulisse.applications.ports.RoutePorts
import ulisse.entities.Route

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object RouteUIInputService:

  object RouteUIInputService:
    def apply(queue: LinkedBlockingQueue[RouteManager => RouteManager]): RoutePorts.UIInputPort =
      RouteUIInputServiceImpl(queue)

    private case class RouteUIInputServiceImpl(queue: LinkedBlockingQueue[RouteManager => RouteManager])
        extends RoutePorts.UIInputPort():

      override def save(optRoute: Option[Route]): Future[Either[RouteManager.ErrorSaving, List[Route]]] =
        val promise = Promise[Either[RouteManager.ErrorSaving, List[Route]]]()
        queue.offer(manager => {
          val either: Either[ErrorSaving, RouteManager] =
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
