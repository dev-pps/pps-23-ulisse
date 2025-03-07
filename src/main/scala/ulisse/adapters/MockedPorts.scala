package ulisse.adapters

import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, WaitingTime}
import ulisse.entities.train.{Trains, Wagons}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime

import scala.concurrent.Future

object MockedPorts:
  private val emptyRightListFuture = (req: String) => {
    println("Request $req")
    Future.successful(Right(List.empty))
  }

  import ulisse.applications.ports.TimetablePorts
  case class TimetableServiceMock() extends TimetablePorts.Input:
    def createTimetable(
        trainName: String,
        departureTime: ClockTime,
        stations: List[(StationId, WaitingTime)]
    ): Future[RequestResult] = emptyRightListFuture("create timetable")
    def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
      emptyRightListFuture("delete timetable")
    def timetablesOf(trainName: String): Future[RequestResult] = emptyRightListFuture("timetableOf")

  import ulisse.applications.ports.TrainPorts
  case class TrainServiceMock() extends TrainPorts.Input:
    def trains: Future[List[Train]] =
      val avTech: TrainTechnology =
        TrainTechnology("AV", maxSpeed = 300, acceleration = 1.5, deceleration = 1.0)
      val wagonInfo: Wagons.Wagon = Wagons.PassengerWagon(300)
      val AV1000Train: Train      = Train(name = "AV1000", avTech, wagonInfo, length = 4)
      val AV800Train: Train       = Train(name = "AV800", avTech, wagonInfo, length = 12)
      val trains                  = List(AV1000Train, AV800Train)
      Future.successful(trains)
    def technologies: Future[List[TrainTechnology]] =
      Future.successful(List.empty)
    def wagonTypes: Future[List[UseType]] = Future.successful(Wagons.UseType.values.toList)
    def createTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]] = emptyRightListFuture("create train")

    def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]] =
      emptyRightListFuture(s"remove train $trainName")
    def updateTrain(name: String)(
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]] = emptyRightListFuture(s"update train $name")
