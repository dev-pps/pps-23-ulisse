package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.infrastructures.view.timetable.TimetableViewControllers.Error.{
  EmptyStationField,
  RequestException,
  TimetableSaveError
}
import ulisse.utils.Times.{ClockTime, Time}
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
    def trainNames: List[String]
    def requestTimetable(trainName: String, time: Time): Unit
    def selectTrain(trainName: String): Unit
    def setDepartureTime(h: Int, m: Int): Unit
    def insertStation(stationName: String, waitTime: Option[Int]): Either[Error, List[TimetableEntry]]
    def undoLastInsert(): List[TimetableEntry]
    def insertedStations(): List[TimetableEntry]
    def reset(): List[TimetableEntry]
    def save(): Unit

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
        import Error.{EmptyDepartureTime, EmptyTrainSelection}
        for
          _           <- selectedTrain.toRight(EmptyTrainSelection())
          departTime  <- startTime.toRight(EmptyDepartureTime())
          stationName <- validateNonBlankString(stationName, EmptyStationField())
        yield
          val departString = Option.when(stations.isEmpty)(s"${departTime.h}:${departTime.m}")
          stations = stations.appended(TableEntryData(stationName, None, departString, waitTime))
          stations

      override def undoLastInsert(): List[TimetableEntry] =
        stations = stations.dropRight(1)
        stations

      override def trainNames: List[String] = List("Rv-3908", "AV-1000", "RV-2020")
      override def save(): Unit =
        println("request port to save timetbale")
        for
          trainName     <- selectedTrain
          departureTime <- startTime
        yield
          val res = port.createTimetable(trainName, departureTime, stations.map(e => (e.name, e.waitMinutes)))
          res.onComplete {
            case Failure(exc)       => println(RequestException(exc.getMessage))
            case Success(Left(err)) => println(TimetableSaveError(s"$err"))
            case Success(Right(_)) =>
              reset()
              // TODO: show on gui that timetable is saved
              println(s"updated list of timetables: done")
          }

      override def requestTimetable(trainName: String, time: Time): Unit =
        port.timetablesOf(trainName).onComplete {
          case Failure(exc)       => RequestException(exc.getMessage)
          case Success(Left(err)) => println(s"SERVICE error: $err")
          case Success(Right(l)) =>
            l.find(_.departureTime.asTime == time).map: tt =>
              val timetable = tt.table.map((st, t) =>
                TableEntryData(st.name, t.arriving.map(_.toString), t.departure.map(_.toString), t.waitTime)
              ).toList
          // TODO: notify view to show selected timetable
        }

      override def reset(): List[TimetableEntry] =
        selectedTrain = None
        startTime = None
        stations = List.empty
        stations

      override def insertedStations(): List[TimetableEntry] = stations
      override def selectTrain(trainName: String): Unit     = selectedTrain = Some(trainName)
      override def setDepartureTime(h: Int, m: Int): Unit   = startTime = ClockTime(h, m).toOption
