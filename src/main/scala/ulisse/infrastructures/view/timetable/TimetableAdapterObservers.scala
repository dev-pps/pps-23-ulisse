package ulisse.infrastructures.view.timetable

/** Pattern Observer traits of timetable views. */
object TimetableAdapterObservers:

  trait Observed[T]:
    /** Register observer interested to show timetable draft not yet saved. */
    def addListener(observer: Updatable[T]): Unit

    /** Register observer interested to be notified of errors. */
    def addErrorObserver(errObserver: ErrorObserver): Unit

  /** Observer interested to show errors. */
  trait ErrorObserver:
    /** Shows error with given `title` and `description` */
    def showError(title: String, descr: String): Unit

  /** Generic updatable observer interested to get `data` updates. */
  trait Updatable[T]:
    def update(data: T): Unit
