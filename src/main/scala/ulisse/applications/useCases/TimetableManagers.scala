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

  trait AcceptanceContextPolicy:
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
    TimetableManager(Map.empty)

  trait TimetableManager:
    def save(timetable: TrainTimetable)(using
        acceptancePolicy: AcceptanceContextPolicy
    ): Either[TimetableManagerErrors, TimetableManager]
//    def delete(trainName: String, departureTime: ClockTime): Either[TimetableNotFound, TimetableManager]
    def tablesOf(trainName: String): Either[TimetableNotFound, List[TrainTimetable]]

  object TimetableManager:
    def apply(timetables: Map[Train, List[TrainTimetable]]): TimetableManager =
      TimetableManagerImpl(timetables)

    private case class TimetableManagerImpl(timetables: Map[Train, List[TrainTimetable]]) extends TimetableManager:

      override def save(timetable: TrainTimetable)(using
          acceptancePolicy: AcceptanceContextPolicy
      ): Either[TimetableManagerErrors, TimetableManager] =
        for
          ts <- tablesOf(timetable.train.name).orElse(Right(List.empty))
          t  <- acceptancePolicy.accept(timetable, ts)
        yield TimetableManager(timetables.updatedWith(t.train) {
          case Some(l) => Some(l.appended(t))
          case None    => Some(List(t))
        })

//      override def delete(train: Train, departureTime: ClockTime): Either[TimetableNotFound, TimetableManager] =
//        for
//          trainTimetables <- tablesOf(train.name)
//          table <- trainTimetables.find(t => t.departureTime === departureTime).toRight(TimetableNotFound(
//            s"not table found with ${train.name} and departure time $departureTime"
//          ))
//        yield timetables.updatedWith(train)()

      override def tablesOf(trainName: String): Either[TimetableNotFound, List[TrainTimetable]] =
        timetables.find((k, _) => k.name.contentEquals(trainName)).map(_._2).toRight(TimetableNotFound(trainName))
