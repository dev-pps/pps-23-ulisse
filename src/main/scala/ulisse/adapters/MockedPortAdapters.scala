package ulisse.adapters

import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, WaitingTime}
import ulisse.entities.train.{Trains, Wagons}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime

import scala.concurrent.Future

/** Contains some mocked implementation of service ports. */
object MockedPortAdapters:
  private val emptyRightListFuture = (req: String) => {
    println("Request $req")
    Future.successful(Right(List.empty))
  }

  import ulisse.applications.ports.TimetablePorts

  /** Mocked TimetableService that returns always Right of an empty list and prints name of request. */
  case class TimetableServiceMock() extends TimetablePorts.Input:
    override def createTimetable(
        trainName: String,
        departureTime: ClockTime,
        stations: List[(StationId, WaitingTime)]
    ): Future[RequestResult] = emptyRightListFuture("create timetable")
    override def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
      emptyRightListFuture("delete timetable")
    override def timetablesOf(trainName: String): Future[RequestResult] = emptyRightListFuture("timetableOf")

  import ulisse.applications.ports.TrainPorts

  /** Mocked TrainService that returns always Right of an empty list and prints name of request.
    *
    * In case of methods `trains`, `technologies` and `wagonTypes` returns some mocked entities.
    */
  case class TrainServiceMock() extends TrainPorts.Input:
    private val avTech: TrainTechnology =
      TrainTechnology("AV", maxSpeed = 300, acceleration = 1.5, deceleration = 1.0)
    override def trains: Future[List[Train]] =
      val wagonInfo: Wagons.Wagon = Wagons.PassengerWagon(300)
      val AV1000Train: Train      = Train(name = "AV1000", avTech, wagonInfo, length = 4)
      val AV800Train: Train       = Train(name = "AV800", avTech, wagonInfo, length = 12)
      val trains                  = List(AV1000Train, AV800Train)
      Future.successful(trains)
    override def technologies: Future[List[TrainTechnology]] =
      Future.successful(List(avTech))
    override def wagonTypes: Future[List[UseType]] = Future.successful(Wagons.UseType.values.toList)
    override def createTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]] = emptyRightListFuture("create train")

    override def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]] =
      emptyRightListFuture(s"remove train $trainName")
    override def updateTrain(name: String)(
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]] = emptyRightListFuture(s"update train $name")
