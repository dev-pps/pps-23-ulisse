package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.applications.ports.RoutePorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object RouteService:
  def apply[N: Numeric, C <: Coordinate[N]](queue: LinkedBlockingQueue[RouteManager[N, C] => RouteManager[N, C]])
      : RoutePorts.Input[N, C] = RouteServiceImpl(queue)

  private case class RouteServiceImpl[N: Numeric, C <: Coordinate[N]](
      queue: LinkedBlockingQueue[RouteManager[N, C] => RouteManager[N, C]]
  ) extends RoutePorts.Input[N, C]:

    override def save(route: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]] =
      val promise = Promise[Either[Errors, List[Route[N, C]]]]()
      queue.offer(manager => {
        manager.save(route) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future

    override def modify(oldRoute: Route[N, C], newRoute: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]] =
      val promise = Promise[Either[Errors, List[Route[N, C]]]]()
      queue.offer(manager => {
        manager.modify(oldRoute, newRoute) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future

    override def delete(route: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]] =
      val promise = Promise[Either[Errors, List[Route[N, C]]]]()
      queue.offer(manager => {
        manager.delete(route) match
          case Left(error)       => promise.success(error.asLeft); manager
          case Right(newManager) => promise.success(newManager.routes.asRight); newManager
      })
      promise.future
