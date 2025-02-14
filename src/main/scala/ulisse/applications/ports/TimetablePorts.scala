package ulisse.applications.ports

import ulisse.entities.timetable.Timetables.TrainTimetable
import ulisse.utils.Errors.ErrorMessage
import ulisse.utils.Times.ClockTime
import scala.concurrent.Future

object TimetablePorts:

  type StationId     = String
  type RequestResult = Either[TimetableServiceErrors, List[TrainTimetable]]

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
    /** Requests to create a new timetable for a `trainName` train that stops or transit in some stations.
      * @param trainName
      *   Train's name
      * @param departureTime
      *   Departure time of train from first station
      * @param stations
      *   Ordered list of stations where first element is starting station, last one is arriving.
      * @return
      *   Returns updated list of timetable otherwise a [[TimetableServiceErrors]]
      */
    def createTimetable(
        trainName: String,
        departureTime: ClockTime,
        stations: List[(StationId, Option[Int])]
    ): Future[RequestResult]

    /** Deletes timetable identified by `trainName` and `departureTime`. If table is found is deleted, otherwise is
      * returned an error.
      * @param trainName
      *   Train's name
      * @param departureTime
      *   [[ClockTime]]
      * @return
      *   Returns updated list of train's Timetables otherwise a [[TimetableServiceErrors]]
      */
    def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult]

    /** @param trainName
      *   Train's name
      * @return
      *   List of train's Timetables
      */
    def timetableOf(trainName: String): Future[RequestResult]
