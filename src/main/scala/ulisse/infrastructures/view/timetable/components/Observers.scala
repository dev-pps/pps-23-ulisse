package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry

object Observers:
  trait ErrorObserver:
    def showError(descr: String): Unit

  trait Updatable[T]:
    def update(data: T): Unit

  trait UpdatablePreview       extends Updatable[List[TimetableEntry]]
  trait UpdatableTimetableView extends Updatable[List[TimetableEntry]]
