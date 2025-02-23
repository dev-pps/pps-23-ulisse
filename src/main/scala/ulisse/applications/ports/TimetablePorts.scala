package ulisse.applications.ports

import ulisse.entities.timetable.Timetables.Timetable
import ulisse.utils.Errors.ErrorMessage
import ulisse.utils.Times.{ClockTime, Time}
import scala.concurrent.Future

object TimetablePorts:

  type StationId     = String
  type WaitingTime   = Option[Int]
  type RequestResult = Either[TimetableServiceErrors, List[Timetable]]

  import ulisse.utils.Errors.{BaseError, ErrorNotExist, ErrorValidation}
  trait TimetableServiceErrors extends BaseError

  object TimetableServiceErrors:
    final case class TrainTablesNotExist(trainName: String) extends ErrorNotExist(s"train $trainName")
        with TimetableServiceErrors
    final case class OverlapError(reason: String)
        extends ErrorValidation(s"train timetable not saved: $reason")
        with TimetableServiceErrors
    final case class InvalidStationSelection(reason: String) extends ErrorValidation(reason) with TimetableServiceErrors
    final case class GenericError(reason: String)            extends ErrorMessage(reason) with TimetableServiceErrors

  trait Input:
    /**   Returns updated list of timetables when `trainName`, `departureTime` and `stations`
      * (list of stations where first element is starting station, last one is arriving).
      *
      *   Returns a [[TimetableServiceErrors]] if any of param is invalid.
      */
    def createTimetable(
        trainName: String,
        departureTime: ClockTime,
        stations: List[(StationId, WaitingTime)]
    ): Future[RequestResult]

    /** Returns updated list of train's Timetables with removed train identified by `trainName` and `departureTime` otherwise a [[TimetableServiceErrors]]. */
    def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult]

    /** Returns List of train's Timetables when `trainName` is valid, otherwise a [[TimetableServiceErrors]] */
    def timetablesOf(trainName: String): Future[RequestResult]
