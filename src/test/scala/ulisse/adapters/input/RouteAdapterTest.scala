package ulisse.adapters.input

import cats.data.Chain
import cats.syntax.option.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Runner.runAll
import ulisse.adapters.input.RouteAdapter.{toCreationInfo, RouteCreationInfo}
import ulisse.applications.managers.RouteManagers
import ulisse.applications.useCases.RouteService
import ulisse.applications.{AppState, EventQueue}
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

implicit val customEc: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

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
        for route <- validateRoute
        yield
          val response = adapter save (Option.empty, route.toCreationInfo)
          updateState()
          Await result (response, Duration.Inf) mustBe Right(singleElementManager.routes)

      "launch an error when save same route" in:
        for route <- validateRoute
        yield
          val save       = adapter.save(Option.empty, route.toCreationInfo)
          val repeatSave = adapter.save(Option.empty, route.toCreationInfo)
          updateState()
          Await result (repeatSave, Duration.Inf) mustBe Left(Chain(RouteManagers.Errors.AlreadyExist))

      "replace the route when inputs are valid and oldRoute is not empty" in:
        for
          route         <- validateRoute
          updateRoute   <- validateDifferentRoute
          updateManager <- singleElementManager modify (route, updateRoute)
        yield
          val responseSave   = adapter.save(Option.empty, route.toCreationInfo)
          val responseUpdate = adapter.save(route.some, updateRoute.toCreationInfo)
          updateState()
          Await result (responseUpdate, Duration.Inf) mustBe Right(updateManager.routes)

      "launch an error when update route that not exist" in:
        for route <- validateRoute
        yield
          val update = adapter.save(route.some, route.toCreationInfo)
          updateState()
          Await result (update, Duration.Inf) mustBe Left(Chain(RouteManagers.Errors.NotFound))

      "launch an errors when inputs are not valid" in:
        for
          route <- validateRoute
          error <- validateDifferentRoute
        yield
          val errorSameDepartureStation = adapter.save(Option.empty, route.toCreationInfo.copy(arrival = departure))
          val errorSameArrivalStation   = adapter.save(Option.empty, route.toCreationInfo.copy(departure = arrival))
          val errorFewRails             = adapter.save(Option.empty, route.toCreationInfo.copy(rails = "0"))
          val errorTooManyRails         = adapter.save(Option.empty, route.toCreationInfo.copy(rails = "100"))
          val errorTooShort             = adapter.save(Option.empty, route.toCreationInfo.copy(length = "0"))
          updateState()
          (Await result (errorSameDepartureStation, Duration.Inf)) mustBe Left(Chain(Routes.Errors.SameStation))
          (Await result (errorSameArrivalStation, Duration.Inf)) mustBe Left(Chain(Routes.Errors.SameStation))
          (Await result (errorFewRails, Duration.Inf)) mustBe Left(Chain(Routes.Errors.FewRails))
          (Await result (errorTooManyRails, Duration.Inf)) mustBe Left(Chain(Routes.Errors.TooManyRails))
          (Await result (errorTooShort, Duration.Inf)) mustBe Left(Chain(Routes.Errors.TooShort))

    "click delete route" should:
      "delete the route when inputs are valid" in:
        for route <- validateRoute
        yield
          val save   = adapter.save(Option.empty, route.toCreationInfo)
          val delete = adapter.delete(route.toCreationInfo)
          updateState()
          Await result (save, Duration.Inf)
          Await result (delete, Duration.Inf) mustBe Right(List.empty)

      "launch an error when delete route that not exist" in:
        for route <- validateRoute
        yield
          val delete = adapter.delete(route.toCreationInfo)
          updateState()
          Await result (delete, Duration.Inf) mustBe Left(Chain(RouteManagers.Errors.NotExist))
