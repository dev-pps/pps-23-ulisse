package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.Runner.runAll

class QuestStateTest extends AnyFlatSpec with Matchers:
  import AppStateTest.*

  private val initialState  = AppState()
  private val eventQueue    = EventQueue()
  private def updateState() = runAll(initialState, eventQueue.events)

  "add read station event" should "update station manager" in:
    eventQueue.addReadStationEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create station event" should "update station manager" in:
    eventQueue.addCreateStationEvent(updateStation)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateStation(updateStation))

  "add update station event" should "update railway network" in:
    eventQueue.addUpdateStationEvent(updateStationSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateStationSchedule(updateStationSchedule))

  "add delete station event" should "update station schedule" in:
    eventQueue.addDeleteStationEvent(updateStationSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateStationSchedule(updateStationSchedule))

  "add read route event" should "update route manager" in:
    eventQueue.addReadRouteEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create route event" should "update railway network" in:
    eventQueue.addCreateRouteEvent(updateRailwayNetwork)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwayNetwork(updateRailwayNetwork))

  "add update route event" should "update railway network" in:
    eventQueue.addUpdateRouteEvent(updateRailwayNetwork)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRailwayNetwork(updateRailwayNetwork))

  "add delete route event" should "update route schedule" in:
    eventQueue.addDeleteRouteEvent(updateRouteSchedule)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateRouteSchedule(updateRouteSchedule))

  "add read train event" should "update train manager" in:
    eventQueue.addReadTrainEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create train event" should "update train manager" in:
    eventQueue.addCreateTrainEvent(updateTrain)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateTrain(updateTrain))

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

  "add read simulation event" should "update simulation manager" in:
    eventQueue.addReadSimulationEvent(_ => ())
    val states = updateState()
    states.lastOption mustBe Some(initialState)

  "add create simulation event" should "update simulation manager" in:
    eventQueue.addCreateSimulationEvent(initSimulation)
    val states = updateState()
    states.lastOption mustBe Some(initialState.initSimulation(initSimulation))

  "add update simulation event" should "update simulation manager" in:
    eventQueue.addUpdateSimulationEvent(initSimulation)
    val states = updateState()
    states.lastOption mustBe Some(initialState.initSimulation(initSimulation))

  "add delete simulation event" should "update simulation manager" in:
    eventQueue.addDeleteSimulationEvent(updateSimulation)
    val states = updateState()
    states.lastOption mustBe Some(initialState.updateSimulation(updateSimulation))
