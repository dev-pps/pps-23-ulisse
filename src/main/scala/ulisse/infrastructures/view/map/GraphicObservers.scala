package ulisse.infrastructures.view.map

object GraphicObservers:
  def createObservable[T]: GraphicObservable[T] = Observable(List.empty)

  trait GraphicObserver[T]:
    def onClick(data: T): Unit
    def onHover(data: T): Unit
    def onRelease(data: T): Unit

  trait GraphicObservable[T]:
    def attach(observer: GraphicObserver[T]): Unit
    def detach(observer: GraphicObserver[T]): Unit

    def notifyOnClick(data: T): Unit
    def notifyOnHover(data: T): Unit
    def notifyOnRelease(data: T): Unit

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class Observable[T](var observers: List[GraphicObserver[T]]) extends GraphicObservable[T]:
    override def attach(observer: GraphicObserver[T]): Unit = observers = observers.appended(observer)
    override def detach(observer: GraphicObserver[T]): Unit = observers = observers.filterNot(_ == observer)

    override def notifyOnClick(data: T): Unit   = observers.foreach(_.onClick(data))
    override def notifyOnHover(data: T): Unit   = observers.foreach(_.onHover(data))
    override def notifyOnRelease(data: T): Unit = observers.foreach(_.onRelease(data))
