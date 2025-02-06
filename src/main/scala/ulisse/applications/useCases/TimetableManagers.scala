package ulisse.applications.useCases

import ulisse.applications.useCases.TimetableManagers.TimetableManagerErrors.{AcceptanceError, TimetableNotFound}
import ulisse.entities.timetable.Timetables.TrainTimetable
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist}
import ulisse.utils.Times.ClockTime

object TimetableManagers:

  trait TimetableManagerErrors extends BaseError
  object TimetableManagerErrors:
    final case class AcceptanceError(reason: String) extends ErrorMessage(s"Timetable not approved: $reason")
        with TimetableManagerErrors
    final case class TimetableNotFound(trainName: String)
        extends ErrorNotExist(s"No timetables exist for train $trainName") with TimetableManagerErrors

  /** A rules specification for acceptance checks of new `timetable`. Checks are done by method `accept`.
    */
  trait AcceptanceContextPolicy:
    /** @param timetable
      *   [[TrainTimetable]] to check.
      * @param tables
      *   all TrainTimetables used by acceptance policy.
      * @return
      *   returns the timetable otherwise if timetable not pass policy rules is returned an [[AcceptanceError]]
      */
    def accept(timetable: TrainTimetable, tables: List[TrainTimetable]): Either[AcceptanceError, TrainTimetable]

  private object DefaultAcceptancePolicy extends AcceptanceContextPolicy:
    override def accept(
        newTimetable: TrainTimetable,
        trainTables: List[TrainTimetable]
    ): Either[AcceptanceError, TrainTimetable] =
      /* other optional checks are:
      - new Train timetable starting station should be an arriving station of another timetable (ClockTime should not be overlapped)
      - new train timetable arriving station should be an starting station of another timetable
      (timetables in that check are concatenable)
       */
      trainTables.find(t =>
        val r =
          for
            arriving    <- t.arrivingTime
            newArriving <- newTimetable.arrivingTime
          yield newTimetable.departureTime > arriving || newArriving >= t.departureTime
        r.getOrElse(false)
      ).map(_ => AcceptanceError("Overlapped timetable: train not available")).toLeft(newTimetable)

  given defaultAcceptancePolicy: AcceptanceContextPolicy = DefaultAcceptancePolicy

  def emptyManager(): TimetableManager =
    TimetableManager(List.empty)

  trait TimetableManager:
    /** Save new timetable for a train. Timetable is accepted if passes the `acceptancePolicy` rules.
      * @param timetable
      *   `TrainTimetable` to add
      * @param acceptancePolicy
      *   `AcceptanceContextPolicy` that defines timetable acceptance rules
      * @return
      *   `Right` of updated `TimetableManager`, in case of error and `Left` of `TimetableManagerErrors`
      */
    def save(timetable: TrainTimetable)(using
        acceptancePolicy: AcceptanceContextPolicy
    ): Either[TimetableManagerErrors, TimetableManager]

    /** Removes train's timetable identified by `trainName` and `departureTime`
      * @param trainName
      *   Train name
      * @param departureTime
      *   departure time of train
      * @return
      *   Updated TimetableManager otherwise Left of [[TimetableNotFound]]
      */
    def remove(trainName: String, departureTime: ClockTime): Either[TimetableNotFound, TimetableManager]

    /** Gets all timetables of a train.
      * @param trainName
      *   Train's name
      * @return
      *   If al least one timetable is saved returns List of TrainTimetables, otherwise a Left of `TimetableNotFound`
      */
    def tablesOf(trainName: String): Either[TimetableNotFound, List[TrainTimetable]]

  object TimetableManager:
    def apply(timetables: List[TrainTimetable]): TimetableManager =
      TimetableManagerImpl(timetables.groupBy(_.train))

    private case class TimetableManagerImpl(timetables: Map[Train, List[TrainTimetable]]) extends TimetableManager:

      override def save(timetable: TrainTimetable)(using
          acceptancePolicy: AcceptanceContextPolicy
      ): Either[TimetableManagerErrors, TimetableManager] =
        for
          ts <- tablesOf(timetable.train.name).orElse(Right(List.empty))
          t  <- acceptancePolicy.accept(timetable, ts)
        yield TimetableManagerImpl(timetables.updatedWith(t.train) {
          case Some(l) => Some(l.appended(t))
          case None    => Some(List(t))
        })

      override def remove(trainName: String, departureTime: ClockTime): Either[TimetableNotFound, TimetableManager] =
        for
          trainTimetables <- tablesOf(trainName)
          train           <- timetables.keys.find(_.name.contentEquals(trainName)).toRight(TimetableNotFound(trainName))
          table <- trainTimetables.find(t => t.departureTime === departureTime).toRight(TimetableNotFound(
            s"not table found with $trainName and departure time $departureTime"
          ))
          updatedTimetables <- Right(timetables.updatedWith(train) {
            case Some(t) if t.sizeIs > 1 => Some(trainTimetables.filter(_ == table))
            case _                       => None
          })
        yield TimetableManagerImpl(updatedTimetables)

      override def tablesOf(trainName: String): Either[TimetableNotFound, List[TrainTimetable]] =
        timetables.find((k, _) => k.name.contentEquals(trainName)).map(_._2).toRight(TimetableNotFound(trainName))
