package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.timetable.subviews.Observers.*
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.{
  generateMockTimetable,
  TableEntryData,
  TimetableEntry
}
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateNonBlankString

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.swing.Swing
import scala.util.{Failure, Success}

object TimetableViewControllers:
  sealed trait Error(val title: String, val descr: String) extends BaseError
  object Error:
    final case class EmptyTrainSelection(action: String) extends Error(action, "Empty train field")
    final case class EmptyDepartureTime(action: String)  extends Error(action, "Departure time empty")
    final case class EmptyStationField(action: String)   extends Error(action, "Station not set")
    final case class TimetableSaveError(msg: String)
        extends Error("Timetable Save request Error", msg)
    final case class RequestException(excMsg: String) extends Error("Request Exception", s"message: $excMsg")

  trait TimetableViewController extends Observed:
    def trainNames: List[String]
    def requestTimetables(trainName: String): Unit
    def selectTrain(trainName: String): Unit
    def setDepartureTime(h: Int, m: Int): Unit
    def insertStation(stationName: String, waitTime: Option[Int]): Unit
    def undoLastInsert(): Unit
    def insertedStations(): List[TimetableEntry]
    def reset(): Unit
    def save(): Unit
    def deleteTimetable(trainName: Option[String], selectedTime: Option[ClockTime]): Unit

  object TimetableViewController:
    def apply(port: TimetablePorts.Input): TimetableViewController =
      ViewControllerImpl(port)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private class ViewControllerImpl(port: TimetablePorts.Input)
        extends TimetableViewController:
      import ulisse.infrastructures.view.timetable.TimetableViewControllers.Error.*
      private var stations: List[TimetableEntry]                = List.empty
      private var selectedTrain: Option[String]                 = None
      private var startTime: Option[ClockTime]                  = None
      private var timetablePreview: Option[UpdatablePreview]    = None
      private var timetableView: Option[UpdatableTimetableView] = None
      private var errorObserver: Option[ErrorObserver]          = None

      given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
        Executors.newFixedThreadPool(1)
      )

      private def updatePreview(): Unit =
        timetablePreview.foreach(o => Swing.onEDT(o.update(stations)))

      private def showError(err: Error): Unit =
        errorObserver.foreach(o => Swing.onEDT(o.showError(err.title, err.descr)))

      override def trainNames: List[String] = List("Rv-3908", "AV-1000", "RV-2020")

      override def insertStation(stationName: String, waitTime: Option[Int]): Unit =
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
          case Left(err) => showError(err)
          case Right(updatedStations) =>
            stations = updatedStations
            updatePreview()

      override def undoLastInsert(): Unit =
        stations = stations.dropRight(1)
        updatePreview()

      override def deleteTimetable(trainName: Option[String], departureTime: Option[ClockTime]): Unit =
        for
          t       <- trainName
          depTime <- departureTime
        yield port.deleteTimetable(t, depTime).handleOnComplete: updatedTimetables =>
          timetableView.map(_.update(updatedTimetables))

      override def save(): Unit =
        println("request port to save timetable")
        val errorTitle = "Save timetable"
        val res =
          for
            trainName     <- selectedTrain.toRight(EmptyTrainSelection(errorTitle))
            departureTime <- startTime.toRight(EmptyDepartureTime(errorTitle))
          yield port.createTimetable(trainName, departureTime, stations.map(e => (e.name, e.waitMinutes)))

        res match
          case Left(err) => showError(TimetableSaveError(err.descr))
          case Right(fs) => fs.handleOnComplete(_ => reset())

      override def requestTimetables(trainName: String): Unit =
        port.timetablesOf(trainName).onComplete {
          case Failure(exc)               => showError(RequestException(exc.getMessage))
          case Success(Left(err))         => showError(RequestException(s"SERVICE: $err"))
          case Success(Right(timetables)) => timetableView.map(_.update(timetables))
        }

      override def reset(): Unit =
        selectedTrain = None
        startTime = None
        stations = List.empty
        updatePreview()

      override def insertedStations(): List[TimetableEntry] = stations
      override def selectTrain(trainName: String): Unit =
        selectedTrain.foreach(_ => reset())
        selectedTrain = Some(trainName)
      override def setDepartureTime(h: Int, m: Int): Unit =
        startTime.foreach(_ => reset())
        startTime = ClockTime(h, m).toOption

      override def addTimetableViewListener(timetableViewer: UpdatableTimetableView): Unit =
        timetableView = Some(timetableViewer)
      override def addPreviewListener(previewObserver: UpdatablePreview): Unit =
        timetablePreview = Some(previewObserver)
      override def addErrorObserver(errObserver: ErrorObserver): Unit =
        errorObserver = Some(errObserver)

      import ulisse.applications.ports.TimetablePorts.RequestResult
      extension (toComplete: Future[RequestResult])
        private def handleOnComplete(onSuccess: List[Timetable] => Unit): Unit =
          toComplete.onComplete {
            case Failure(e)         => showError(RequestException(e.getMessage))
            case Success(Left(err)) => showError(RequestException(s"SERVICE: $err"))
            case Success(Right(r))  => onSuccess(r)
          }
