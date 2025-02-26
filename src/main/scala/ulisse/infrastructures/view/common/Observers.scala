package ulisse.infrastructures.view.common

/** Represents the observer pattern. */
object Observers:

  /** Creates an observable of the pattern. */
  def createObservable[T]: Observable[T] = ObservableImpl(List.empty)

  /** Represents the observer of the pattern. */
  trait Observer[T]:
    /** Called when the observer is clicked, given the data [[T]] of the event. */
    def onClick(data: T): Unit = ()

    /** Called when the observer is released, given the data [[T]] of the event. */
    def onRelease(data: T): Unit = ()

    /** Called when the observer is hovered, given the data [[T]] of the event. */
    def onHover(data: T): Unit = ()

    /** Called when the observer is exited, given the data [[T]] of the event. */
    def onExit(data: T): Unit = ()

  /** Represents the observable of the pattern. */
  trait Observable[T]:
    /** Returns the list of observers. */
    def observers: List[Observer[T]]

    /** Attaches an observer to the observable. */
    def attach(observer: Observer[T]): Unit

    /** Detaches an observer from the observable. */
    def detach(observer: Observer[T]): Unit

    /** Notifies the observers that the observable is clicked, given the data [[T]] of the event. */
    def notifyClick(data: T): Unit

    /** Notifies the observers that the observable is released, given the data [[T]] of the event. */
    def notifyRelease(data: T): Unit

    /** Notifies the observers that the observable is hovered, given the data [[T]] of the event. */
    def notifyHover(data: T): Unit

    /** Notifies the observers that the observable is exited, given the data [[T]] of the event. */
    def notifyExit(data: T): Unit

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class ObservableImpl[T](private var list: List[Observer[T]]) extends Observable[T]:
    override def observers: List[Observer[T]] = list

    override def attach(observer: Observer[T]): Unit = list = observers.appended(observer)
    override def detach(observer: Observer[T]): Unit = list = observers.filterNot(_ == observer)

    override def notifyClick(data: T): Unit   = observers.foreach(_.onClick(data))
    override def notifyHover(data: T): Unit   = observers.foreach(_.onHover(data))
    override def notifyRelease(data: T): Unit = observers.foreach(_.onRelease(data))
    override def notifyExit(data: T): Unit    = observers.foreach(_.onExit(data))
