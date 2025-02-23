package ulisse.applications.managers

import ulisse.applications.managers.TimetableManagers.TimetableManagerErrors.{AcceptanceError, TimetableNotFound}
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist}
import ulisse.utils.Times.{===, >=, ClockTime, Time}

object TimetableManagers:

  /** Errors that can returned by manager */
  trait TimetableManagerErrors extends BaseError
  object TimetableManagerErrors:
    final case class AcceptanceError(reason: String) extends ErrorMessage(s"Timetable not approved: $reason")
        with TimetableManagerErrors
    final case class TimetableNotFound(trainName: String)
        extends ErrorNotExist(s"No timetables exist for train $trainName") with TimetableManagerErrors

  /** A rules specification for accepting new `timetable`. Checks are done by method `accept`. */
  trait AcceptanceTimetablePolicy:
    /** Returns the `timetable` otherwise if is not accepted by policy rules is returned an [[AcceptanceError]].
      * Param `tables` is a list of TrainTimetable used by acceptance policy for checks
      */
    def accept(timetable: Timetable, tables: List[Timetable]): Either[AcceptanceError, Timetable]

  /** Default acceptance policy of new timetables. New timetable are accepted if there is no overlapping between tables. */
  private object NoOverlappingTimePolicy extends AcceptanceTimetablePolicy:
    override def accept(
        newTimetable: Timetable,
        trainTables: List[Timetable]
    ): Either[AcceptanceError, Timetable] =
      def isNotOverlapping(t: Timetable): Boolean =
        val overlaps =
          for
            arriving    <- t.arrivingTime
            newArriving <- newTimetable.arrivingTime
          yield newTimetable.departureTime >= arriving || t.departureTime >= newArriving
        overlaps.getOrElse(false)

      if trainTables.isEmpty || trainTables.exists(isNotOverlapping) then
        Right(newTimetable)
      else
        Left(AcceptanceError("Overlapped timetable: train not available"))

  given defaultAcceptancePolicy: AcceptanceTimetablePolicy = NoOverlappingTimePolicy

  /** Return an empty manager */
  def emptyManager(): TimetableManager =
    TimetableManager(List.empty)

  trait TimetableManager:
    /** Save new `timetable` for a train. Timetable is accepted if passes the `acceptancePolicy` rules.
      * Returns `Right` of updated `TimetableManager` otherwise `Left` of `TimetableManagerErrors` in case of errors.
      */
    def save(timetable: Timetable)(using
        acceptancePolicy: AcceptanceTimetablePolicy
    ): Either[TimetableManagerErrors, TimetableManager]

    /** Removes train's timetable identified by `trainName` and `departureTime`.
      * Returns an updated TimetableManager otherwise Left of [[TimetableNotFound]]
      */
    def remove(trainName: String, departureTime: ClockTime): Either[TimetableNotFound, TimetableManager]

    /** Gets all timetables of a given `trainName`
      * If al least one timetable is saved returns List of TrainTimetables, otherwise a Left of `TimetableNotFound`
      */
    def tablesOf(trainName: String): Either[TimetableNotFound, List[Timetable]]

  object TimetableManager:
    /** Returns TimetableManager initialized with given list of `timetables`. The `timetables` are not checked by the
      * [[AcceptanceTimetablePolicy]].
      */
    def apply(timetables: List[Timetable]): TimetableManager =
      TimetableManagerImpl(timetables.groupBy(_.train))

    /** Returns new TimetableManager initialized with a given `timetables` map. */
    def fromMap(timetables: Map[Train, List[Timetable]]): TimetableManager =
      TimetableManagerImpl(timetables)

    private case class TimetableManagerImpl(timetables: Map[Train, List[Timetable]]) extends TimetableManager:

      override def save(timetable: Timetable)(using
          acceptancePolicy: AcceptanceTimetablePolicy
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

      override def tablesOf(trainName: String): Either[TimetableNotFound, List[Timetable]] =
        timetables.find((k, _) => k.name.contentEquals(trainName)).map(_._2).toRight(TimetableNotFound(trainName))
