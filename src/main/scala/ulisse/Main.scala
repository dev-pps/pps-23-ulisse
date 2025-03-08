package ulisse

import ulisse.adapters.InputAdapterManager
import ulisse.adapters.input.{SimulationInfoAdapter, SimulationPageAdapter}
import ulisse.adapters.output.SimulationNotificationAdapter
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.ports.{SimulationInfoPorts, SimulationPorts}
import ulisse.applications.useCases.{SimulationInfoService, SimulationService}
import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.infrastructures.utilty.SimulationNotificationBridge
import ulisse.infrastructures.view.GUI
import ulisse.infrastructures.view.page.workspaces.SimulationWorkspace
import ulisse.infrastructures.view.simulation.SimulationNotificationListener
import ulisse.utils.Times.FluentDeclaration.h

object Main:

  def main(args: Array[String]): Unit =
    launchApp()

  private final case class SimulationSetting(eventQueue: EventQueue):
    private val simulationBridge: SimulationNotificationBridge = SimulationNotificationBridge(() => workspace)
    private val simulationOutput: SimulationPorts.Output       = SimulationNotificationAdapter(simulationBridge)
    val simulationInput: SimulationPorts.Input                 = SimulationService(eventQueue, simulationOutput)
    val simulationInfoInput: SimulationInfoPorts.Input         = SimulationInfoService(eventQueue)
    val simulationAdapter: SimulationPageAdapter               = SimulationPageAdapter(simulationInput)
    val simulationInfoAdapter: SimulationInfoAdapter           = SimulationInfoAdapter(simulationInfoInput)

    val workspace: SimulationWorkspace = SimulationWorkspace(simulationAdapter, simulationInfoAdapter)

  @main def launchApp(): Unit =
    val eventQueue      = EventQueue()
    val simulationSetup = SimulationSetting(eventQueue)
    val inputPortManager =
      InputPortManager(eventQueue, simulationSetup.simulationInput, simulationSetup.simulationInfoInput)
    val inputAdapterManager =
      InputAdapterManager(inputPortManager, simulationSetup.simulationAdapter, simulationSetup.simulationInfoAdapter)

    val gui = GUI(inputAdapterManager, simulationSetup.workspace)

    // TRAIN
    val defaultTechnology     = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
    val normalTrainTechnology = TrainTechnology("Normal", 100, 0.5, 0.25)
    val technologyManager     = TechnologyManager(List(defaultTechnology, normalTrainTechnology))
    val defaultWagon          = Wagon(UseType.Passenger, 5)
    val defaultWagonNumber    = 1
    val train3905             = Train("3905", normalTrainTechnology, defaultWagon, defaultWagonNumber)
    val train3906             = Train("3906", normalTrainTechnology, defaultWagon, defaultWagonNumber)

    // STATION
    val stationA = Station("A", Coordinate(409, 188), 1)
    val stationB = Station("B", Coordinate(758, 377), 1)
    val stationC = Station("C", Coordinate(505, 672), 1)

    // ROUTE
    val routeA = Route(stationA, stationB, RouteType.Normal, 1, 450).toOption
    val routeB = Route(stationB, stationC, RouteType.Normal, 1, 450).toOption

    // RAIL
    val routeTEOa: RailInfo = RailInfo(length = 450, typeRoute = RouteType.Normal)
    val routeTEOb: RailInfo = RailInfo(length = 450, typeRoute = RouteType.Normal)

    // TIME-TABLE
    val timetable1: Timetable =
      TimetableBuilder(
        train = train3906,
        startStation = stationC,
        departureTime = h(8).m(0).getOrDefault
      ).stopsIn(stationB, waitTime = 0)(routeTEOa)
        .arrivesTo(stationA)(routeTEOb)

    // TIME-TABLE
    val timetable2: Timetable =
      TimetableBuilder(
        train = train3905,
        startStation = stationA,
        departureTime = h(8).m(0).getOrDefault
      ).stopsIn(stationB, waitTime = 0)(routeTEOa)
        .arrivesTo(stationC)(routeTEOb)

    val initialState = AppState.withTechnology(technologyManager)

    val newState1 = initialState.updateStationManager(manager =>
      manager.addStation(stationA).flatMap(_.addStation(stationB).flatMap(_.addStation(stationC))).getOrElse(manager)
    )

    val newState2 = newState1.updateRoute(manager =>
      // RICORDA FOLD LEFT
      val routes = (routeA, routeB)
      routes match
        case (Some(a), Some(b)) => manager.save(a).flatMap(_.save(b)).getOrElse(manager)
        case _                  => manager
    )

    val newState3 = newState2.updateTrain((trainManager, technologyManager) =>
      trainManager.addTrain(train3905).getOrElse(trainManager)
    )

    val newState4 = newState3.updateTimetable(manager =>
      val e = manager.save(timetable2)
      println(e)
      val r = e.getOrElse(manager).save(timetable1)
      println(r)
      r.getOrElse(manager)
    )

    eventQueue.startProcessing(newState4)
