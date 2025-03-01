package ulisse.infrastructures.view.timetable.subviews

import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry
import ulisse.utils.Times.Time

object Observers:

  trait Observed:
    def addTimetableViewListener(timetableViewer: UpdatableTimetableView): Unit
    def addPreviewListener(previewObserver: UpdatablePreview): Unit
    def addErrorObserver(errObserver: ErrorObserver): Unit

  trait ErrorObserver:
    def showError(title: String, descr: String): Unit

  trait Updatable[T]:
    def update(data: T): Unit

  trait UpdatablePreview       extends Updatable[List[TimetableEntry]]
  trait UpdatableTimetableView extends Updatable[List[Timetable]]
