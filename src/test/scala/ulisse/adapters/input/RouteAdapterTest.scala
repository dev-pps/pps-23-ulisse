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
    "insert invalid data for saving and deletion" should:
      "notify an error not choose departure" in:
        for route <- validateRoute
        yield
          val creationInfo = route.toCreationInfo.copy(departure = None)
          val saveError    = adapter.save(Option.empty, creationInfo)
          val deleteError  = adapter.delete(creationInfo)
          updateState()
          val error = Left(Chain(RouteAdapter.Errors.NotChooseDeparture))
          Await result (saveError, Duration.Inf) mustBe error
          Await result (deleteError, Duration.Inf) mustBe error

      "notify an error not choose arrival" in:
        for route <- validateRoute
        yield
          val creationInfo = route.toCreationInfo.copy(arrival = None)
          val saveError    = adapter.save(Option.empty, creationInfo)
          val deleteError  = adapter.delete(creationInfo)
          updateState()
          val error = Left(Chain(RouteAdapter.Errors.NotChooseArrival))
          Await result (saveError, Duration.Inf) mustBe error
          Await result (deleteError, Duration.Inf) mustBe error

      "notify an error invalid route type" in:
        for route <- validateRoute
        yield
          val creationInfo = route.toCreationInfo.copy(typology = "Invalid")
          val saveError    = adapter.save(Option.empty, creationInfo)
          val deleteError  = adapter.delete(creationInfo)
          updateState()
          val error = Left(Chain(RouteAdapter.Errors.InvalidRouteType))
          Await result (saveError, Duration.Inf) mustBe error
          Await result (deleteError, Duration.Inf) mustBe error

      "notify an error because railsCount is not a number" in:
        for route <- validateRoute
        yield
          val creationInfo = route.toCreationInfo.copy(rails = "Invalid")
          val saveError    = adapter.save(Option.empty, creationInfo)
          val deleteError  = adapter.delete(creationInfo)
          updateState()
          val error = Left(Chain(RouteAdapter.Errors.InvalidRailsCount))
          Await result (saveError, Duration.Inf) mustBe error
          Await result (deleteError, Duration.Inf) mustBe error

      "notify an error because length is not a number" in:
        for route <- validateRoute
        yield
          val creationInfo = route.toCreationInfo.copy(length = "Invalid")
          val saveError    = adapter.save(Option.empty, creationInfo)
          val deleteError  = adapter.delete(creationInfo)
          updateState()
          val error = Left(Chain(RouteAdapter.Errors.InvalidRouteLength))
          Await result (saveError, Duration.Inf) mustBe error
          Await result (deleteError, Duration.Inf) mustBe error

    "click save route" should:
      "add a new route when inputs are valid and oldRoute is empty" in:
        for route <- validateRoute
        yield
          val response = adapter save (Option.empty, route.toCreationInfo)
          updateState()
          Await result (response, Duration.Inf) mustBe Right(singleElementManager.routes)

      "notify an error when save same route" in:
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

      "notify an error when update route that not exist" in:
        for route <- validateRoute
        yield
          val update = adapter.save(route.some, route.toCreationInfo)
          updateState()
          Await result (update, Duration.Inf) mustBe Left(Chain(RouteManagers.Errors.NotFound))

      "notify an errors when inputs are not valid" in:
        for
          route <- validateRoute
          error <- validateDifferentRoute
        yield
          val errorSameDepartureStation =
            adapter.save(Option.empty, route.toCreationInfo.copy(arrival = departure.some))
          val errorSameArrivalStation = adapter.save(Option.empty, route.toCreationInfo.copy(departure = arrival.some))
          val errorFewRails           = adapter.save(Option.empty, route.toCreationInfo.copy(rails = "0"))
          val errorTooManyRails       = adapter.save(Option.empty, route.toCreationInfo.copy(rails = "100"))
          val errorTooShort           = adapter.save(Option.empty, route.toCreationInfo.copy(length = "0"))
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

      "notify an error when delete route that not exist" in:
        for route <- validateRoute
        yield
          val delete = adapter.delete(route.toCreationInfo)
          updateState()
          Await result (delete, Duration.Inf) mustBe Left(Chain(RouteManagers.Errors.NotExist))
