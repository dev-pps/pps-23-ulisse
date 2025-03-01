package ulisse.infrastructures.view.timetable.subviews

import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry

/** Pattern Observer traits of timetable views. */
object Observers:

  trait Observed:
    /** Register observer interested to show selected saved timetable. */
    def addTimetableViewListener(timetableViewer: UpdatableTimetableView): Unit

    /** Register observer interested to show timetable draft not yet saved. */
    def addPreviewListener(previewObserver: UpdatablePreview): Unit

    /** Register observer interested to be notified of errors. */
    def addErrorObserver(errObserver: ErrorObserver): Unit

  /** Observer interested to show errors. */
  trait ErrorObserver:
    /** Shows error with given `title` and `description` */
    def showError(title: String, descr: String): Unit

  /** Generic updatable observer interested to get `data` updates. */
  trait Updatable[T]:
    def update(data: T): Unit

  /** Updatable timetable preview observer. An updatable observer where updated data is `List[TimetableEntry]` */
  trait UpdatablePreview extends Updatable[List[TimetableEntry]]

  /** Updatable View that shows saved Timetables. An updatable observer where updated data is `List[Timetable]` */
  trait UpdatableTimetableView extends Updatable[List[Timetable]]
