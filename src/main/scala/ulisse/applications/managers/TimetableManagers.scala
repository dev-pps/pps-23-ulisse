package ulisse.applications.managers

import ulisse.applications.managers.TimetableManagers.TimetableManagerErrors.{
  AcceptanceError,
  DeletionError,
  TimetableNotFound
}
import ulisse.entities.route.Routes
import ulisse.entities.station.Station
import ulisse.entities.route.Routes.Route
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist}
import ulisse.utils.Times.{===, >=, ClockTime, Time}

object TimetableManagers:

  /** Responsible to guarantee consistent deletion of [[Timetable]] when entities
    * as [[Route]], [[Station]] or [[Train]] are deleted.
    */
  trait DeletionListener:

    /** Deletes all timetables related to `train` otherwise an error is returned */
    def trainDeleted(train: Train): Either[TimetableManagerErrors, TimetableManager]

    /** Deletes all timetables containing `station` otherwise an error is returned */
    def stationDeleted(station: Station): Either[TimetableManagerErrors, TimetableManager]

    /** Deletes all timetables containing `route` otherwise an error is returned */
    def routeDeleted(route: Route): Either[TimetableManagerErrors, TimetableManager]

  /** Listener for entities updates, it is  Responsible to guarantee consistent updates
    * of [[Timetable]] when entities as [[Route]], [[Station]] or [[Train]] are updated.
    */
  trait UpdateListener:
    /** Updates timetables and recalculates times of all tables related to given `train`. */
    def trainUpdated(train: Train): Either[TimetableManagerErrors, TimetableManager]

    /** Updates timetables and recalculates times of all tables that contains `oldStation` with the new one `newStation` */
    def stationUpdated(oldStation: Station, newStation: Station): Either[TimetableManagerErrors, TimetableManager]

    /** Updates timetables and recalculates times of all tables that contains `oldRoute` with the `newRoute`. */
    def routeUpdated(oldRoute: Route, newRoute: Route): Either[TimetableManagerErrors, TimetableManager]

  /** Errors that can returned by manager */
  trait TimetableManagerErrors extends BaseError
  object TimetableManagerErrors:
    final case class AcceptanceError(reason: String) extends ErrorMessage(s"Timetable not approved: $reason")
        with TimetableManagerErrors
    final case class TimetableNotFound(trainName: String)
        extends ErrorNotExist(s"No timetables exist for train $trainName") with TimetableManagerErrors
    final case class DeletionError(descr: String) extends ErrorMessage(s"Delete error: $descr")
        with TimetableManagerErrors

  /** A rules specification for accepting new `timetable`. Checks are done by method `accept`. */
  trait AcceptanceTimetablePolicy:
    /** Returns the `timetable` otherwise if is not accepted by policy rules is returned an [[AcceptanceError]].
      * Param `tables` is a list of Timetable used by acceptance policy for checks
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
  def empty(): TimetableManager = TimetableManager(List.empty)

  trait TimetableManager extends DeletionListener with UpdateListener:
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

    /** Returns sequence of timetables. */
    def tables: Seq[Timetable]

    /** Gets all timetables of a given `trainName`
      * If al least one timetable is saved returns List of Timetables, otherwise a Left of `TimetableNotFound`
      */
    def tablesOf(trainName: String): Either[TimetableNotFound, List[Timetable]]

  object TimetableManager:
    /** Returns TimetableManager initialized with given list of `timetables`. The `timetables` are checked by the
      * [[AcceptanceTimetablePolicy]].
      */
    def apply(timetables: List[Timetable]): TimetableManager =
      fromMap(timetables.groupBy(_.train))

    /** Returns new TimetableManager initialized with a given `timetables` map.
      *
      * If one Timetable overlaps with another one (according to the departure time), the last computed one is discarded.
      * Given timetables are checked by the [[AcceptanceTimetablePolicy]].
      */
    def fromMap(timetables: Map[Train, List[Timetable]]): TimetableManager =
      val validatedTimetables =
        timetables.map: (train, tableList) =>
          train -> tableList.sortBy(_.departureTime).foldLeft(List.empty[Timetable]): (validated, t) =>
            defaultAcceptancePolicy.accept(t, validated) match
              case Left(_)  => validated
              case Right(t) => t :: validated
      TimetableManagerImpl(validatedTimetables)

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

      override def tables: Seq[Timetable] = timetables.values.toList.flatten
      override def tablesOf(trainName: String): Either[TimetableNotFound, List[Timetable]] =
        timetables.find((k, _) => k.name.contentEquals(trainName)).map(_._2).toRight(TimetableNotFound(trainName))

      override def trainDeleted(train: Train): Either[TimetableManagerErrors, TimetableManager] =
        timetables.get(train).toRight(DeletionError(s"train ${train.name} not found")).map(_ =>
          TimetableManagerImpl(timetables.removed(train))
        )

      override def stationDeleted(station: Station): Either[TimetableManagerErrors, TimetableManager] =
        val updatedTimetables =
          timetables.map((train, tables) => (train, tables.filterNot(_.stations.contains(station))))
        deletionResult(updatedTimetables, s"timetables with station $station not found")

      override def routeDeleted(route: Routes.Route): Either[TimetableManagerErrors, TimetableManager] =
        val railInfo         = Some(RailInfo(route.length, route.typology))
        val routeToDelete    = (route.departure, route.arrival, railInfo)
        val routeToDeleteInv = (route.arrival, route.departure, railInfo)
        val updatedTimetables = timetables.map((t, tables) =>
          (t, tables.filterNot(tt => tt.routes.contains(routeToDeleteInv) || tt.routes.contains(routeToDelete)))
        )
        deletionResult(updatedTimetables, s"timetables with route $route not found")

      private def deletionResult(
          updatedTables: Map[Train, List[Timetable]],
          errMsg: String
      ): Either[TimetableManagerErrors, TimetableManager] =
        Either.cond(
          updatedTables != timetables,
          TimetableManagerImpl(updatedTables),
          DeletionError(errMsg)
        )

      override def trainUpdated(train: Train): Either[TimetableManagerErrors, TimetableManager] = Right(this)
      // find timetables with same train name
      // recalculate all timetables starting from

      override def stationUpdated(
          oldStation: Station,
          newStation: Station
      ): Either[TimetableManagerErrors, TimetableManager] = Right(this)

      override def routeUpdated(oldRoute: Route, newRoute: Route): Either[TimetableManagerErrors, TimetableManager] =
        Right(this)
