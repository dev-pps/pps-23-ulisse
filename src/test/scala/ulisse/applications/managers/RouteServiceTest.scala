package ulisse.applications.useCases

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagerTest.*
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.entities.RouteTest

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RouteServiceTest extends AnyFlatSpec with Matchers:
  private val events       = LinkedBlockingQueue[RouteManager => RouteManager]()
  private val routeService = RouteService(events)

  private def updateState() = runAll(emptyManager, events)

  "save route on service" should "add a valid route to the route manager" in:
    validateRoute.foreach(route =>
      val result = routeService.save(route)
      updateState()
      Await.result(result, Duration.Inf) must be(Right(List(route)))
    )

  "save two different route on service" should "add two valid route to the route manager" in:
    for
      route          <- validateRoute
      differentRoute <- validateDifferentRoute
    yield
      val firstSaveResult  = routeService.save(route)
      val secondSaveResult = routeService.save(differentRoute)
      updateState()
      Await.result(secondSaveResult, Duration.Inf) must be(Right(List(route, differentRoute)))

  "modify route on service" should "modify a route in the route manager" in:
    for
      route      <- validateRoute
      equalRoute <- validateEqualRoute
    yield
      val saveResult   = routeService.save(route)
      val modifyResult = routeService.modify(route, equalRoute)
      updateState()
      Await.result(modifyResult, Duration.Inf) must be(Right(List(equalRoute)))

  "delete route on service" should "delete a route in the route manager" in:
    validateRoute.foreach(route =>
      val saveResult   = routeService.save(route)
      val deleteResult = routeService.delete(route)
      updateState()
      Await.result(deleteResult, Duration.Inf) must be(Right(List.empty[RouteTest]))
    )
