package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.events.RouteEventQueue
import ulisse.applications.managers.RouteManagers
import ulisse.applications.managers.RouteManagers.Errors
import ulisse.applications.ports.RoutePorts
import ulisse.entities.route.Routes.Route

import scala.concurrent.{Future, Promise}

/** Companion object for the [[RoutePorts.Input]] class. */
object RouteService:

  /** Creates a [[RoutePorts.Input]] instance. */
  def apply(eventQueue: RouteEventQueue): RoutePorts.Input = RouteServiceImpl(eventQueue)

  private case class RouteServiceImpl(eventQueue: RouteEventQueue) extends RoutePorts.Input:

    override def routes: Future[List[Route]] =
      val promise = Promise[List[Route]]()
      eventQueue.addReadRouteEvent(routeManager => promise success routeManager.routes)
      promise.future

    override def save(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue addCreateRouteEvent ((stationManager, routeManager) => {
        val updatedManager = routeManager save route
        (stationManager, Services updateManager (promise, routeManager, updatedManager, _.routes))
      })
      promise.future

    override def modify(oldRoute: Route, newRoute: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue addUpdateRouteEvent ((stationManager, routeManager, timetable) => {
        val updatedManager  = routeManager modify (oldRoute, newRoute)
        val updateTimeTable = (timetable routeUpdated (oldRoute, newRoute)).toOption.getOrElse(timetable)
        (stationManager, Services updateManager (promise, routeManager, updatedManager, _.routes), updateTimeTable)
      })
      promise.future

    override def delete(route: Route): Future[Either[Errors, List[Route]]] =
      val promise = Promise[Either[Errors, List[Route]]]()
      eventQueue addDeleteRouteEvent ((routeManager, timetableManager) => {
        val updatedManager  = routeManager delete route
        val updateTimetable = timetableManager routeDeleted route
        (Services updateManager (promise, routeManager, updatedManager, _.routes), timetableManager)
      })
      promise.future
