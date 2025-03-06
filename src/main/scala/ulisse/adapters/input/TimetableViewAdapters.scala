package ulisse.adapters.input

import ulisse.applications.ports.{TimetablePorts, TrainPorts}
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.timetable.TimetableViewModel.{trainId, TableEntryData, TimetableEntry, TrainId}
import ulisse.infrastructures.view.timetable.TimetableAdapterObservers.*
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime
import ulisse.utils.ValidationUtils.validateNonBlankString

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.swing.Swing
import scala.util.{Failure, Right as reset, Success}

object TimetableViewAdapters:
  /** Timetable view error. It contains a `title` and a `descr` (description). */
  sealed trait Error(val title: String, val descr: String) extends BaseError
  object Error:
    final case class EmptyTrainSelection(action: String) extends Error(action, "Empty train field")
    final case class EmptyDepartureTime(action: String)  extends Error(action, "Departure time empty")
    final case class EmptyStationField(action: String)   extends Error(action, "Station not set")
    final case class TimetableSaveError(msg: String)
        extends Error("Timetable Save request Error", msg)
    final case class RequestException(excMsg: String) extends Error("Request Exception", s"message: $excMsg")

  /** Timetable view controller.
    *
    * It is observable for list of [[TrainId]]
    */
  trait TimetableViewAdapter extends Observed[List[TrainId]]:
    /** Requests train names. */
    def requestTrainNames(): Unit

    /** Requests timetables given the `trainName` to timetable service. */
    def requestTimetables(trainName: String): Unit

    /** Requests timetable deletion given a `trainName` and `departingTime` to timetable service only
      * if both params are present.
      */
    def deleteTimetable(trainName: Option[String], departingTime: Option[ClockTime]): Unit

    /** Set `trainName` as train in timetable in draft and keep it until timetable draft is saved correctly. */
    def selectTrain(trainName: String): Unit

    /** Set departure time draft of timetable until timetable draft is saved correctly. */
    def setDepartureTime(h: Int, m: Int): Unit

    /** Insert station with `stationName` and `waitTime` to timetable draft. */
    def insertStation(stationName: String, waitTime: Option[Int]): List[TimetableEntry]

    /** Remove last inserted station from timetable draft. */
    def undoLastInsert(): List[TimetableEntry]

    /** Reset timetable draft, forget train name, departure time and all stations. */
    def reset(): List[TimetableEntry]

    /** Save timetable draft. */
    def save(): Unit

  object TimetableViewAdapter:
    /** Creates view controller for the timetables views. It requires `port` to which communicate to save and get required infos. */
    def apply(timetablePort: TimetablePorts.Input, trainPort: TrainPorts.Input): TimetableViewAdapter =
      ViewAdapterImpl(timetablePort, trainPort)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private class ViewAdapterImpl(tablePort: TimetablePorts.Input, trainPort: TrainPorts.Input)
        extends TimetableViewAdapter:
      import TimetableViewAdapters.Error.*
      private var stations: List[TimetableEntry]                     = List.empty
      private var selectedTrain: Option[String]                      = None
      private var startTime: Option[ClockTime]                       = None
      private var errorObserver: Option[ErrorObserver]               = None
      private var trainNamesObserver: List[Updatable[List[TrainId]]] = List.empty

      given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
        Executors.newFixedThreadPool(1)
      )

      private def showError(err: Error): Unit =
        errorObserver.foreach(o => Swing.onEDT(o.showError(err.title, err.descr)))

      override def requestTrainNames(): Unit =
        trainPort.trains.onComplete {
          case Failure(e) => showError(RequestException(e.getMessage))
          case Success(l) => trainNamesObserver.foreach(_.update(l.map(t => trainId(t.name))))
        }

      override def insertStation(stationName: String, waitTime: Option[Int]): List[TimetableEntry] =
        import Error.{EmptyDepartureTime, EmptyTrainSelection}
        val errorTitle = "Insert station"
        val res =
          for
            _           <- selectedTrain.toRight(EmptyTrainSelection(errorTitle))
            departTime  <- startTime.toRight(EmptyDepartureTime(errorTitle))
            stationName <- validateNonBlankString(stationName, EmptyStationField(errorTitle))
          yield
            val departString = Option.when(stations.isEmpty)(s"${departTime.h}:${departTime.m}")
            stations.appended(TableEntryData(stationName, None, departString, waitTime))
        res match
          case Left(err) =>
            showError(err)
            stations
          case Right(updatedStations) =>
            stations = updatedStations
            stations

      override def undoLastInsert(): List[TimetableEntry] =
        stations = stations.dropRight(1)
        stations

      override def deleteTimetable(trainName: Option[String], departureTime: Option[ClockTime]): Unit =
        for
          t       <- trainName
          depTime <- departureTime
        yield tablePort.deleteTimetable(t, depTime).handleOnComplete: updatedTimetables =>
          // TODO: notify the view interested
          println(updatedTimetables)

      override def save(): Unit =
        val errorTitle = "Save timetable"
        val res =
          for
            trainName     <- selectedTrain.toRight(EmptyTrainSelection(errorTitle))
            departureTime <- startTime.toRight(EmptyDepartureTime(errorTitle))
          yield tablePort.createTimetable(trainName, departureTime, stations.map(e => (e.name, e.waitMinutes)))

        res match
          case Left(err) => showError(TimetableSaveError(err.descr))
          case Right(fs) => fs.handleOnComplete(_ => reset())

      override def requestTimetables(trainName: String): Unit =
        tablePort.timetablesOf(trainName).onComplete {
          case Failure(exc)               => showError(RequestException(exc.getMessage))
          case Success(Left(err))         => showError(RequestException(s"SERVICE: $err"))
          case Success(Right(timetables)) =>
            // TODO: notify the view interested
            println(timetables)
        }

      override def reset(): List[TimetableEntry] =
        selectedTrain = None
        startTime = None
        stations = List.empty
        stations

      override def selectTrain(trainName: String): Unit =
        selectedTrain.foreach(_ => reset())
        selectedTrain = Some(trainName)

      override def setDepartureTime(h: Int, m: Int): Unit =
        startTime.foreach(_ => reset())
        startTime = ClockTime(h, m).toOption

      override def addErrorObserver(errObserver: ErrorObserver): Unit =
        errorObserver = Some(errObserver)

      override def addListener(observer: Updatable[List[TrainId]]): Unit =
        trainNamesObserver = observer :: trainNamesObserver

      import ulisse.applications.ports.TimetablePorts.RequestResult
      extension (toComplete: Future[RequestResult])
        private def handleOnComplete(onSuccess: List[Timetable] => Unit): Unit =
          toComplete.onComplete {
            case Failure(e)         => showError(RequestException(e.getMessage))
            case Success(Left(err)) => showError(RequestException(s"SERVICE: $err"))
            case Success(Right(r))  => onSuccess(r)
          }
