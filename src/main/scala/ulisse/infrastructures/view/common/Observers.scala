package ulisse.infrastructures.view.common

object Observers:

  def createObservable[T]: Observable[T] = ObservableImpl(List.empty)

  trait Observer[T]:
    def onClick(data: T): Unit
    def onHover(data: T): Unit
    def onRelease(data: T): Unit

  trait Observable[T]:
    def observers: List[Observer[T]]
    def attach(observer: Observer[T]): Unit
    def detach(observer: Observer[T]): Unit

    def notifyOnClick(data: T): Unit
    def notifyOnHover(data: T): Unit
    def notifyOnRelease(data: T): Unit

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class ObservableImpl[T](private var list: List[Observer[T]]) extends Observable[T]:
    override def observers: List[Observer[T]] = list

    override def attach(observer: Observer[T]): Unit = list = observers.appended(observer)
    override def detach(observer: Observer[T]): Unit = list = observers.filterNot(_ == observer)

    override def notifyOnClick(data: T): Unit   = observers.foreach(_.onClick(data))
    override def notifyOnHover(data: T): Unit   = observers.foreach(_.onHover(data))
    override def notifyOnRelease(data: T): Unit = observers.foreach(_.onRelease(data))
