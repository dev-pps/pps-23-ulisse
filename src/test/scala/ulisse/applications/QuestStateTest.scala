package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

class QuestStateTest extends AnyFlatSpec with Matchers:
  private val initialState  = AppState()
  private val eventQueue    = EventQueue()
  private def updateState() = runAll(initialState, eventQueue.events)

//  "update railway map" should "update the application state" in:
//    val stationManager   = mock[StationManager]
//    val routeManager     = mock[RouteManager]
//    val timetableManager = mock[TimetableManager]
//    val updateMap = (_: StationManager, _: RouteManager, _: TimetableManager) =>
//      (stationManager, routeManager, timetableManager)
//
//    eventQueue.offerUpdateMap(updateMap)
//    val states = updateState()
//
//    states.lastOption mustBe Some(initialState.updateMap(updateMap))
//
//  "update train" should "update the application state" in:
//    val trainManager     = mock[TrainManager]
//    val timetableManager = mock[TimetableManager]
//    val updateTrain      = (_: TrainManager, _: TimetableManager) => (trainManager, timetableManager)
//
//    eventQueue.offerUpdateTrain(updateTrain)
//    val states = updateState()
//
//    states.lastOption mustBe Some(initialState.updateTrain(updateTrain))
