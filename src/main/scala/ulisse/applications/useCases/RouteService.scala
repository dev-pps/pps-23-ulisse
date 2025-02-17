package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.applications.ports.RoutePorts
import ulisse.entities.route.Routes.Route

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object RouteService:
  def apply(queue: LinkedBlockingQueue[RouteManager => RouteManager]): RoutePorts.Input = RouteServiceImpl(queue)

  private case class RouteServiceImpl(
      queue: LinkedBlockingQueue[RouteManager => RouteManager]
  ) extends RoutePorts.Input:

    override def save(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      queue.offer(manager => {
        manager.save(route) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future

    override def modify(oldRoute: Route, newRoute: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      queue.offer(manager => {
        manager.modify(oldRoute, newRoute) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future

    override def delete(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      queue.offer(manager => {
        manager.delete(route) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future
