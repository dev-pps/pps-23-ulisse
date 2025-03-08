package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.Runner.runAll

class EventQueueTest extends AnyFlatSpec with Matchers:
  import AppStateTest.*

  private val initialState  = AppState()
  private val eventQueue    = EventQueue()
  private def updateState() = runAll(initialState, eventQueue.events)

  "add read station manager event" should "not update station manager" in:
    eventQueue.addReadStationManagerEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add update station manager" should "update station manager" in:
    eventQueue.addUpdateStationManagerEvent(updateStationManager)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateStationManager(updateStationManager))

  "add update station managers event" should "update station managers" in:
    eventQueue.addUpdateStationManagersEvent(updateStationManagers)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateStationManagers(updateStationManagers))

  "add read route event" should "update route manager" in:
    eventQueue.addReadRouteEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create route event" should "update railway network" in:
    eventQueue.addCreateRouteEvent(updateRailwayNetwork)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwayNetwork(updateRailwayNetwork))

  "add update route event" should "update railway network" in:
    eventQueue.addUpdateRouteEvent(updateRailwayNetworkSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwayNetworkSchedule(updateRailwayNetworkSchedule))

  "add delete route event" should "update route schedule" in:
    eventQueue.addDeleteRouteEvent(updateRouteSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRouteSchedule(updateRouteSchedule))

  "add read train event" should "update train manager" in:
    eventQueue.addReadTrainEvent((_, _) => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create train event" should "update train manager" in:
    eventQueue.addCreateTrainEvent(createTrain)
    val states = updateState()
    states.lastOption mustBe Some(initialState.createTrain(createTrain))

  "add update train event" should "update train manager" in:
    eventQueue.addUpdateTrainEvent(updateTrain)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateTrain(updateTrain))

  "add delete train event" should "update train schedule" in:
    eventQueue.addDeleteTrainEvent(updateTrainSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateTrainSchedule(updateTrainSchedule))

  "add read timetable event" should "update timetable manager" in:
    eventQueue.addReadTimetableEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create timetable event" should "update railway schedule" in:
    eventQueue.addCreateTimetableEvent(updateRailwaySchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwaySchedule(updateRailwaySchedule))

  "add update timetable event" should "update railway schedule" in:
    eventQueue.addUpdateTimetableEvent(updateRailwaySchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwaySchedule(updateRailwaySchedule))

  "add delete timetable event" should "update timetable manager" in:
    eventQueue.addDeleteTimetableEvent(updateTimetable)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateTimetable(updateTimetable))

  "add read simulation environment event" should "not update simulation environment" in:
    eventQueue.addReadSimulationEnvironmentEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add setup simulation manager event" should "update simulation manager" in:
    eventQueue.addSetupSimulationManagerEvent(setupSimulationManager)
    val states = updateState()
    states.lastOption mustBe Some(initialState.setupSimulationManager(setupSimulationManager))

  "add update simulation manager event" should "update simulation manager" in:
    eventQueue.addUpdateSimulationManagerEvent(updateSimulationManager)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateSimulationManager(updateSimulationManager))
