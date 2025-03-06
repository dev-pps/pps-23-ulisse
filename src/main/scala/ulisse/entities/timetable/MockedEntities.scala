package ulisse.entities.timetable

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, WaitingTime}
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.utils.Times.ClockTime

import scala.concurrent.Future

object MockedEntities:

  import ulisse.applications.ports.TimetablePorts
  case class TimetableInputPortMocked() extends TimetablePorts.Input:
    private val response = Future.successful(Right(List.empty))

    def createTimetable(
        trainName: String,
        departureTime: ClockTime,
        stations: List[(StationId, WaitingTime)]
    ): Future[RequestResult] = response
    def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] = response
    def timetablesOf(trainName: String): Future[RequestResult]                              = response
