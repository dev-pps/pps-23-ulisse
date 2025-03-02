package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.RouteEventQueue
import ulisse.applications.managers.RouteManagers
import ulisse.applications.managers.RouteManagers.{Errors, RouteManager}
import ulisse.applications.ports.RoutePorts
import ulisse.entities.route.Routes.Route

import scala.concurrent.{Future, Promise}

object RouteService:
  def apply(eventQueue: RouteEventQueue): RoutePorts.Input = RouteServiceImpl(eventQueue)

  private case class RouteServiceImpl(eventQueue: RouteEventQueue) extends RoutePorts.Input:

    private def updateState(
        p: Promise[Either[Errors, List[Route]]],
        routeManager: RouteManager,
        updatedManager: Either[Errors, RouteManager]
    ) =
      updatedManager match
        case Left(error: RouteManagers.Errors) => p.success(Left(error)); routeManager
        case Right(value)                      => p.success(Right(value.routes)); value

    override def save(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue.addCreateRouteEvent((stationManager, routeManager) => {
        val updatedManager = routeManager.save(route)
        (stationManager, updateState(promise, routeManager, updatedManager))
      })
      promise.future

    override def modify(oldRoute: Route, newRoute: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue.addUpdateRouteEvent((stationManager, routeManager) => {
        val updatedManager = routeManager.modify(oldRoute, newRoute)
        (stationManager, updateState(promise, routeManager, updatedManager))
      })
      promise.future

    override def delete(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue.addDeleteRouteEvent((routeManager, timetableManager) => {
        val updatedManager = routeManager.delete(route)
        (updateState(promise, routeManager, updatedManager), timetableManager)
      })
      promise.future
