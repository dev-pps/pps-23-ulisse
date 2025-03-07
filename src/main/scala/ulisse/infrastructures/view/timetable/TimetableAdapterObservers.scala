package ulisse.infrastructures.view.timetable

import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.Trains.Train
import ulisse.infrastructures.view.timetable.TimetableViewModel.TrainId

/** Pattern Observer traits of timetable views. */
object TimetableAdapterObservers:

  trait ErrorObservable:
    /** Register observer interested to be notified of errors. */
    def addErrorObserver(errObserver: ErrorObserver): Unit

  trait TrainsObservable:
    def addTrainsObserver(observer: TrainsUpdatable): Unit

  trait TimetablesObservable:
    def addTimetablesObserver(observer: TimetablesUpdatable): Unit

  /** Observer interested to show errors. */
  trait ErrorObserver:
    /** Shows error with given `title` and `description` */
    def showError(title: String, descr: String): Unit

  /** Generic updatable observer interested to get `data` updates. */
  trait Updatable[T]:
    def update(data: T): Unit

  trait TimetablesUpdatable:
    def updateTimetables(tables: List[Timetable]): Unit
  trait TrainsUpdatable:
    def updateNewTrains(trains: List[Train]): Unit
