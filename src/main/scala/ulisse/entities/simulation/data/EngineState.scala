package ulisse.entities.simulation.data

/** Represent the state of the simulation engine */
trait EngineState:

  /** The last update time */
  def lastUpdate: Option[Long]

  /** The last delta time */
  def lastDelta: Long

  /** The elapsed cycle time */
  def elapsedCycleTime: Long

  /** The elapsed cycle time increment */
  def updateElapsedCycleTime(delta: Long): EngineState

  /** function to update the engine state */
  def update(currentUpdate: Long): EngineState

/** Factory for [[EngineState]] instances */
object EngineState:
  val defaultLastUpdate: Option[Long] = None
  val defaultLastDelta: Long          = 0
  val defaultElapsedCycleTime: Long   = 0

  /** Create a new Engine state, allowing only non negative lastUpdate and positive lastDelta and elapsedCycleTime, otherwise substitute the wrong values with defaults */
  def apply(lastUpdate: Option[Long], lastDelta: Long, elapsedCycleTime: Long): EngineState =
    EngineStateImpl(
      lastUpdate.filter(_ > 0),
      math.max(defaultLastDelta, lastDelta),
      math.max(defaultElapsedCycleTime, elapsedCycleTime)
    )

  /** Create a new Engine state with default values */
  def empty(): EngineState = EngineState(defaultLastUpdate, defaultLastDelta, defaultElapsedCycleTime)

  private final case class EngineStateImpl(
      lastUpdate: Option[Long],
      lastDelta: Long,
      elapsedCycleTime: Long
  ) extends EngineState:
    override def updateElapsedCycleTime(delta: Long): EngineState = copy(elapsedCycleTime = elapsedCycleTime + delta)
    override def update(currentUpdate: Long): EngineState = lastUpdate match
      case None => copy(lastUpdate = Some(currentUpdate))
      case Some(lastUpdate) if currentUpdate >= lastUpdate =>
        val deltaElapsed = currentUpdate - lastUpdate
        copy(lastUpdate = Some(currentUpdate), deltaElapsed, elapsedCycleTime + deltaElapsed)
      case _ => this
