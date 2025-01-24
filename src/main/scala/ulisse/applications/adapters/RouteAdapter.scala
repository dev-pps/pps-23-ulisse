package ulisse.applications.adapters

import cats.syntax.either.*
import ulisse.applications.ports.RoutePorts
import ulisse.applications.useCases.RouteManager
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.{Coordinates, Route}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object RouteAdapter:

  object UIAdapter:
    def apply(queue: LinkedBlockingQueue[RouteManager => RouteManager]): RoutePorts.UIPort =
      IUAdapter(queue)

    private case class IUAdapter(queue: LinkedBlockingQueue[RouteManager => RouteManager]) extends RoutePorts.UIPort():

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

//        val responsePort = optRoute.map(route => manager.save(route).map(manager => copy(manager = manager)))
//          .getOrElse(Left(ErrorSaving.creation))
//
//        val newManager: RouteManager => RouteManager = manager => optRoute.map(route => manager.save(route))
//        val newManager: RouteManager => RouteManager = manager => optRoute.map(route => manager.save(route))
//        queue.put(newManager)

//        queue.put(manager => manager.save(optRoute).map(manager => copy(queue = queue)))
//        optRoute.map(route => manager.save(route).map(manager => copy(manager = manager)))
//          .getOrElse(Left(ErrorSaving.creation))
