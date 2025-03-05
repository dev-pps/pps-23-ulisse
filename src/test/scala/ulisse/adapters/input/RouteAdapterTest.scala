package ulisse.adapters.input

import cats.data.Chain
import cats.syntax.option.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers
import ulisse.applications.useCases.RouteService
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Routes.Route.unapply

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RouteAdapterTest extends AnyWordSpec with Matchers:
  import ulisse.applications.managers.RouteManagerTest.*
  import ulisse.entities.route.RouteTest.*

  private val appState   = AppState()
  private val eventQueue = EventQueue()
  private val port       = RouteService(eventQueue)
  private val adapter    = RouteAdapter(port)

  private def updateState() = runAll(appState, eventQueue.events)

  "User" when:
    "click save route" should:

      "add a new route when inputs are valid and oldRoute is empty" in:
        val response = adapter.save(Option.empty, departure, arrival, routeType, railsCount, pathLength)
        updateState()
        Await result (response, Duration.Inf) mustBe Right(singleElementManager.routes)

      "launch an error when save same route" in:
        for route <- validateRoute
        yield
          val save       = adapter.save tupled (Tuple1(Option.empty) ++ unapply(route))
          val repeatSave = adapter.save tupled (Tuple1(Option.empty) ++ unapply(route))
          updateState()
          Await result (repeatSave, Duration.Inf) mustBe Left(RouteManagers.Errors.AlreadyExist)

      "replace the route when inputs are valid and oldRoute is not empty" in:
        for
          route         <- validateRoute
          updateRoute   <- validateDifferentRoute
          updateManager <- singleElementManager modify (route, updateRoute)
        yield
          val responseSave   = adapter.save tupled (Tuple1(Option.empty) ++ unapply(route))
          val responseUpdate = adapter.save tupled (Tuple1(route.some) ++ unapply(updateRoute))
          updateState()
          Await result (responseUpdate, Duration.Inf) mustBe Right(updateManager.routes)

      "launch an errors when inputs are not valid" in:
        for
          route <- validateRoute
          error <- validateDifferentRoute
        yield
          val errorSameStation  = adapter.save(Option.empty, departure, departure, routeType, railsCount, pathLength)
          val errorFewRails     = adapter.save(Option.empty, departure, arrival, routeType, 0, pathLength)
          val errorTooManyRails = adapter.save(Option.empty, departure, arrival, routeType, 100, pathLength)
          val errorTooShort     = adapter.save(Option.empty, departure, arrival, routeType, railsCount, 0)
          updateState()
          (Await result (errorSameStation, Duration.Inf)) mustBe Left(Chain(Routes.Errors.SameStation))
          (Await result (errorFewRails, Duration.Inf)).leftSide mustBe Left(Chain(Routes.Errors.FewRails))
          (Await result (errorTooManyRails, Duration.Inf)).leftSide mustBe Left(Chain(Routes.Errors.TooManyRails))
          (Await result (errorTooShort, Duration.Inf)).leftSide mustBe Left(Chain(Routes.Errors.TooShort))

    "click delete route" should:
      "delete the route when inputs are valid" in:
        for route <- validateRoute
        yield
          val save   = adapter save (Option.empty, departure, arrival, routeType, railsCount, pathLength)
          val delete = adapter delete (departure, arrival, routeType, railsCount, pathLength)
          updateState()
          Await result (save, Duration.Inf)
          Await result (delete, Duration.Inf) mustBe Right(List.empty)

      "launch an error when delete route that not exist" in:
        for route <- validateRoute
        yield
          val delete = adapter.delete(departure, arrival, routeType, railsCount, pathLength)
          updateState()
          Await result (delete, Duration.Inf) mustBe Left(RouteManagers.Errors.NotExist)
