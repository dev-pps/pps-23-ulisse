package ulisse.infrastructures.view.timetable

import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.Trains.Train

/** Pattern Observer traits of timetable views. */
object TimetableAdapterObservers:

  /** Trait that represent */
  trait RequestResultObservable:
    /** Register observer interested to be notified of request results. */
    def addRequestResultObserver(resObserver: RequestResultObserver): Unit

  /** Observer interested to show errors. */
  trait RequestResultObserver:
    /** Shows result of request with given `title` and `description` */
    def showRequestResult(title: String, descr: String): Unit

  /** Observable object able to provide updated list of [[Train]] to [[TrainsUpdatable]] */
  trait TrainsObservable:
    def addTrainsObserver(observer: TrainsUpdatable): Unit

  /** Observable object able to provide updated [[Timetables]] to [[TimetablesUpdatable]] */
  trait TimetablesObservable:
    def addTimetablesObserver(observer: TimetablesUpdatable): Unit

  /** Observer interested to get updated list of [[Timetable]] */
  trait TimetablesUpdatable:
    def updateTimetables(tables: List[Timetable]): Unit

  /** Observer interested to get updated list of [[Train]] */
  trait TrainsUpdatable:
    def updateNewTrains(trains: List[Train]): Unit

  /** Generic updatable observer interested to get `data` updates. */
  trait Updatable[T]:
    def update(data: T): Unit
