package ulisse.applications.useCases

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagerTest.*
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.route.RouteTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RouteServiceTest extends AnyFlatSpec with Matchers:
  private val initState    = AppState()
  private val eventQueue   = EventQueue()
  private val routeService = RouteService(eventQueue)

  private def updateState() = runAll(initState, eventQueue.events)

  "routes on service" should "return the routes from the route manager" in:
    val result = routeService.routes
    updateState()
    Await result (result, Duration.Inf) must be(List.empty[RouteTest])

  "save route and read routes on service" should "return the route from the route manager" in:
    validateRoute foreach (route =>
      val saveResult = routeService save route
      val readResult = routeService.routes
      updateState()
      Await result (readResult, Duration.Inf) must be(List(route))
    )

  "save route on service" should "add a valid route to the route manager" in:
    validateRoute foreach (route =>
      val result = routeService save route
      updateState()
      Await result (result, Duration.Inf) must be(Right(List(route)))
    )

  "save two different route on service" should "add two valid route to the route manager" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield
      val firstSaveResult  = routeService save route
      val secondSaveResult = routeService save differentRoute
      updateState()
      Await result (secondSaveResult, Duration.Inf) must be(Right(List(route, differentRoute)))

  "modify route on service" should "modify a route in the route manager" in:
    for
      route      <- validateRoute
      equalRoute <- validateEqualRoute
    yield
      val saveResult   = routeService save route
      val modifyResult = routeService modify (route, equalRoute)
      updateState()
      Await result (modifyResult, Duration.Inf) must be(Right(List(equalRoute)))

  "delete route on service" should "delete a route in the route manager" in:
    validateRoute foreach (route =>
      val saveResult   = routeService save route
      val deleteResult = routeService delete route
      updateState()
      Await result (deleteResult, Duration.Inf) must be(Right(List.empty[RouteTest]))
    )
