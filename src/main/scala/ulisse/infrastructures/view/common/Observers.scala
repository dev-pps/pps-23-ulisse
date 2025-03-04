package ulisse.infrastructures.view.common

/** Represents the observer pattern. */
object Observers:

  /** Creates an observable of the pattern. */
  def createObservable[T]: Observable[T] =
    ObservableImpl(List.empty, List.empty, List.empty, List.empty, List.empty, List.empty)

  /** Represents the observer for click events. */
  trait ClickObserver[T]:
    /** Called when the observer is clicked, given the data [[T]] of the event. */
    def onClick(data: T): Unit = ()

  /** Represents the observer of the pattern for release events. */
  trait ReleaseObserver[T]:
    /** Called when the observer is released, given the data [[T]] of the event. */
    def onRelease(data: T): Unit = ()

  /** Represents the observer of the pattern for hover events. */
  trait HoverObserver[T]:
    /** Called when the observer is hovered, given the data [[T]] of the event. */
    def onHover(data: T): Unit = ()

  /** Represents the observer of the pattern for exit events. */
  trait ExitObserver[T]:
    /** Called when the observer is exited, given the data [[T]] of the event. */
    def onExit(data: T): Unit = ()

  /** Represents the observer of the pattern for move events. */
  trait MovedObserver[T]:
    /** Called when the observer is moved, given the data [[T]] of the event. */
    def onMove(data: T): Unit = ()

  /** Represents the observer of the pattern. */
  trait Observer[T] extends ClickObserver[T] with ReleaseObserver[T] with HoverObserver[T] with ExitObserver[T]
      with MovedObserver[T]

  /** Represents the observable of the pattern. */
  trait Observable[T]:
    /** Returns the list of observers. */
    def observers: List[Observer[T]]

    /** Returns the list of click observers. */
    def clicks: List[ClickObserver[T]]

    /** Returns the list of release observers. */
    def releases: List[ReleaseObserver[T]]

    /** Returns the list of hover observers. */
    def hovers: List[HoverObserver[T]]

    /** Returns the list of exit observers. */
    def exits: List[ExitObserver[T]]

    /** Returns the list of move observers. */
    def moves: List[MovedObserver[T]]

    /** Attaches an observer to the observable. */
    def attach(observer: Observer[T]): Unit

    /** Detaches an observer from the observable. */
    def detach(observer: Observer[T]): Unit

    /** Detach all observers from the observable. */
    def detachAll(): Unit =
      observers.foreach(detach)
      detachAllClicks()
      detachAllReleases()
      detachAllHovers()
      detachAllExits()
      detachAllMoves()

    /** Attach a click observer to the observable. */
    def attachClick(observer: ClickObserver[T]): Unit

    /** Detach a click observer from the observable. */
    def detachClick(observer: ClickObserver[T]): Unit

    /** Detach all click observers from the observable. */
    def detachAllClicks(): Unit = clicks.foreach(detachClick)

    /** Attach a release observer to the observable. */
    def attachRelease(observer: ReleaseObserver[T]): Unit

    /** Detach a release observer from the observable. */
    def detachRelease(observer: ReleaseObserver[T]): Unit

    /** Detach all release observers from the observable. */
    def detachAllReleases(): Unit = releases.foreach(detachRelease)

    /** Attach a hover observer to the observable. */
    def attachHover(observer: HoverObserver[T]): Unit

    /** Detach a hover observer from the observable. */
    def detachHover(observer: HoverObserver[T]): Unit

    /** Detach all hover observers from the observable. */
    def detachAllHovers(): Unit = hovers.foreach(detachHover)

    /** Attach an exit observer to the observable. */
    def attachExit(observer: ExitObserver[T]): Unit

    /** Detach an exit observer from the observable. */
    def detachExit(observer: ExitObserver[T]): Unit

    /** Detach all exit observers from the observable. */
    def detachAllExits(): Unit = exits.foreach(detachExit)

    /** Attach a move observer to the observable. */
    def attachMove(observer: MovedObserver[T]): Unit

    /** Detach a move observer from the observable. */
    def detachMove(observer: MovedObserver[T]): Unit

    /** Detach all move observers from the observable. */
    def detachAllMoves(): Unit = moves.foreach(detachMove)

    /** Notifies the observers that the observable is clicked, given the data [[T]] of the event. */
    def notifyClick(data: T): Unit

    /** Notifies the observers that the observable is released, given the data [[T]] of the event. */
    def notifyRelease(data: T): Unit

    /** Notifies the observers that the observable is hovered, given the data [[T]] of the event. */
    def notifyHover(data: T): Unit

    /** Notifies the observers that the observable is exited, given the data [[T]] of the event. */
    def notifyExit(data: T): Unit

    /** Notifies the observers that the observable is moved, given the data [[T]] of the event. */
    def notifyMove(data: T): Unit

    /** Converts the observable of type [[T]] to an observer of type [[I]]. */
    def toObserver[I](newData: I => T): Observer[I] =
      new Observer[I]:
        override def onClick(data: I): Unit   = notifyClick(newData(data))
        override def onRelease(data: I): Unit = notifyRelease(newData(data))
        override def onHover(data: I): Unit   = notifyHover(newData(data))
        override def onExit(data: I): Unit    = notifyExit(newData(data))
        override def onMove(data: I): Unit    = notifyMove(newData(data))

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class ObservableImpl[T](
      private var _observers: List[Observer[T]],
      private var _click: List[ClickObserver[T]],
      private var _release: List[ReleaseObserver[T]],
      private var _hover: List[HoverObserver[T]],
      private var _exit: List[ExitObserver[T]],
      private var _move: List[MovedObserver[T]]
  ) extends Observable[T]:
    override def observers: List[Observer[T]]       = _observers
    override def clicks: List[ClickObserver[T]]     = _click
    override def releases: List[ReleaseObserver[T]] = _release
    override def hovers: List[HoverObserver[T]]     = _hover
    override def exits: List[ExitObserver[T]]       = _exit
    override def moves: List[MovedObserver[T]]      = _move

    override def attach(observer: Observer[T]): Unit = _observers = observer :: observers
    override def detach(observer: Observer[T]): Unit = _observers = observers.filterNot(_ == observer)

    override def attachClick(observer: ClickObserver[T]): Unit = _click = observer :: clicks
    override def detachClick(observer: ClickObserver[T]): Unit = _click = clicks.filterNot(_ == observer)

    override def attachRelease(observer: ReleaseObserver[T]): Unit = _release = observer :: releases
    override def detachRelease(observer: ReleaseObserver[T]): Unit = _release = releases.filterNot(_ == observer)

    override def attachHover(observer: HoverObserver[T]): Unit = _hover = observer :: hovers
    override def detachHover(observer: HoverObserver[T]): Unit = _hover = hovers.filterNot(_ == observer)

    override def attachExit(observer: ExitObserver[T]): Unit = _exit = observer :: exits
    override def detachExit(observer: ExitObserver[T]): Unit = _exit = exits.filterNot(_ == observer)

    override def attachMove(observer: MovedObserver[T]): Unit = _move = observer :: moves
    override def detachMove(observer: MovedObserver[T]): Unit = _move = moves.filterNot(_ == observer)

    override def notifyClick(data: T): Unit   = (observers ++ clicks).foreach(_ onClick data)
    override def notifyHover(data: T): Unit   = (observers ++ hovers).foreach(_ onHover data)
    override def notifyRelease(data: T): Unit = (observers ++ releases).foreach(_ onRelease data)
    override def notifyExit(data: T): Unit    = (observers ++ exits).foreach(_ onExit data)
    override def notifyMove(data: T): Unit    = (observers ++ moves).foreach(_ onMove data)
