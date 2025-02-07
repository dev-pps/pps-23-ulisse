package ulisse.infrastructures.view.map

object ViewObservers:

  def createObservable[T]: ViewObservable[T] = Observable(List.empty)

  trait ViewObserver[T]:
    def onClick(data: T): Unit
    def onHover(data: T): Unit
    def onRelease(data: T): Unit

  trait ViewObservable[T]:
    def observers: List[ViewObserver[T]]
    def attach(observer: ViewObserver[T]): Unit
    def detach(observer: ViewObserver[T]): Unit

    def notifyOnClick(data: T): Unit
    def notifyOnHover(data: T): Unit
    def notifyOnRelease(data: T): Unit

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class Observable[T](private var list: List[ViewObserver[T]]) extends ViewObservable[T]:
    override def observers: List[ViewObserver[T]] = list

    override def attach(observer: ViewObserver[T]): Unit = list = observers.appended(observer)
    override def detach(observer: ViewObserver[T]): Unit = list = observers.filterNot(_ == observer)

    override def notifyOnClick(data: T): Unit   = observers.foreach(_.onClick(data))
    override def notifyOnHover(data: T): Unit   = observers.foreach(_.onHover(data))
    override def notifyOnRelease(data: T): Unit = observers.foreach(_.onRelease(data))
