package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.infrastructures.view.timetable.TimetableViewControllers.Error.{
  EmptyStationField,
  RequestException,
  TimetableSaveError
}
import ulisse.utils.Times.ClockTime
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.{
  generateMockTimetable,
  TableEntryData,
  TimetableEntry
}
import ulisse.utils.Errors.{BaseError, ErrorMessage}
import ulisse.utils.ValidationUtils.validateNonBlankString
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object TimetableViewControllers:
  sealed trait Error extends BaseError
  object Error:
    final case class EmptyTrainSelection()             extends Error with ErrorMessage("Any train selected")
    final case class EmptyDepartureTime()              extends Error with ErrorMessage("Departure time empty")
    final case class EmptyStationField()               extends Error with ErrorMessage("Station not set")
    final case class TimetableSaveError(descr: String) extends Error with ErrorMessage(s"error: $descr")
    final case class RequestException(excMsg: String)  extends Error with ErrorMessage(s"message: $excMsg")

  trait TimetableViewController:
    def insertStation(stationName: String, waitTime: Option[Int]): Either[Error, List[TimetableEntry]]
    def undoLastInsert(): Unit
    def trainNames: List[String]
    def selectTrain(trainName: String): Unit
    def setDepartureTime(h: Int, m: Int): Unit
    def save(): Unit
    def insertedStations(): List[TimetableEntry]
    def reset(): Unit

  object TimetableViewController:
    def apply(port: TimetablePorts.Input): TimetableViewController =
      ViewControllerImpl(port, generateMockTimetable(0))

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private class ViewControllerImpl(port: TimetablePorts.Input, private var stations: List[TimetableEntry])
        extends TimetableViewController:
      private var selectedTrain: Option[String] = None
      private var startTime: Option[ClockTime]  = None

      given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
        Executors.newFixedThreadPool(1)
      )

      override def insertStation(stationName: String, waitTime: Option[Int]): Either[Error, List[TimetableEntry]] =
        println("insert station")
        import Error.{EmptyDepartureTime, EmptyTrainSelection}
        for
          _           <- selectedTrain.toRight(EmptyTrainSelection())
          departTime  <- startTime.toRight(EmptyDepartureTime())
          stationName <- validateNonBlankString(stationName, EmptyStationField())
        yield
          val departString = Option.when(stations.isEmpty)(s"${departTime.h}:${departTime.m}")
          stations = TableEntryData(stationName, None, departString, waitTime) :: stations
          stations

      override def undoLastInsert(): Unit =
        stations = stations.dropRight(1)
        // TODO: update timetable preview list view
        println(s"undoLastInsert: $stations")

      override def trainNames: List[String] = List("Rv-3908", "AV-1000", "RV-2020")
      override def save(): Unit =
        println("request port to save timetbale")
        for
          trainName     <- selectedTrain
          departureTime <- startTime
        yield
          val res = port.createTimetable(trainName, departureTime, stations.map(e => (e.name, e.waitMinutes)))
          res.onComplete {
            case Failure(exc) => println(RequestException(exc.getMessage))
            case Success(value) => value match
                case Left(err) =>
                  // TODO: show error
                  println(TimetableSaveError(s"$err"))
                case Right(l) =>
                  // TODO: update gui
                  println(s"updated list of timetables: $l")
          }

      override def reset(): Unit =
        selectedTrain = None
        startTime = None
        stations = List.empty
      override def insertedStations(): List[TimetableEntry] = stations
      override def selectTrain(trainName: String): Unit     = selectedTrain = Some(trainName)
      override def setDepartureTime(h: Int, m: Int): Unit   = startTime = ClockTime(h, m).toOption
