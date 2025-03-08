package ulisse.entities

import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.RailInfo
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

object TestMockedEntities:
  val trainTechnology: TrainTechnology = TrainTechnology("AV", maxSpeed = 300, acceleration = 1.5, deceleration = 1.0)
  val wagonInfo: Wagons.Wagon          = Wagons.PassengerWagon(300)
  val AV1000Train: Train               = Train(name = "AV1000", trainTechnology, wagonInfo, length = 4)
  val AV800Train: Train                = Train(name = "AV800", trainTechnology, wagonInfo, length = 12)
  val railAV_10: RailInfo              = RailInfo(length = 10, typeRoute = AV)

  val stationA: Station = Station("Station A", Coordinate(0, 0), 1)
  val stationB: Station = Station("Station B", Coordinate(10, 0), 1) // 2 min from A
  val stationC: Station = Station("Station C", Coordinate(25, 0), 1) // 3 min from B
  val stationD: Station = Station("Station D", Coordinate(50, 0), 1) // 5 min from C
  val stationF: Station = Station("Station F", Coordinate(55, 0), 1) // 1 min from D

  val appState: AppState = AppState()
    .updateTechnology(_ => TechnologyManager(List(trainTechnology)))
    .createTrain((_, _) => TrainManager(List(AV1000Train, AV800Train)))
