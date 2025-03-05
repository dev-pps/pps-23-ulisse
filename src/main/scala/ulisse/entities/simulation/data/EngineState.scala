package ulisse.entities.simulation.data

/** Represent the state of the simulation engine */
trait EngineState:

  /** The last update time */
  def lastUpdate: Option[Double]

  /** The last delta time */
  def lastDelta: Double

  /** The elapsed cycle time */
  def elapsedCycleTime: Double

  /** The elapsed cycle time increment */
  def updateElapsedCycleTime(delta: Double): EngineState

  /** function to update the engine state */
  def update(currentUpdate: Double): EngineState

/** Factory for [[EngineState]] instances */
object EngineState:
  val defaultLastUpdate: Option[Double] = None
  val defaultLastDelta: Double = 0
  val defaultElapsedCycleTime: Double = 0

  /** Create a new Engine state, allowing only non negative lastUpdate and positive lastDelta and elapsedCycleTime, otherwise substitute the wrong values with defaults*/
  def apply(lastUpdate: Option[Double], lastDelta: Double, elapsedCycleTime: Double): EngineState =
    EngineStateImpl(lastUpdate.filter(_ > 0), math.max(defaultLastDelta, lastDelta), math.max(defaultElapsedCycleTime, elapsedCycleTime))

  /** Create a new Engine state with default values */
  def empty(): EngineState = EngineState(defaultLastUpdate, defaultLastDelta, defaultElapsedCycleTime)

  private final case class EngineStateImpl(
      lastUpdate: Option[Double],
      lastDelta: Double,
      elapsedCycleTime: Double
  ) extends EngineState:
    override def updateElapsedCycleTime(delta: Double): EngineState = copy(elapsedCycleTime = elapsedCycleTime + delta)
    override def update(currentUpdate: Double): EngineState = lastUpdate match
      case None => copy(lastUpdate = Some(currentUpdate))
      case Some(lastUpdate) if currentUpdate >= lastUpdate =>
        val deltaElapsed = currentUpdate - lastUpdate
        copy(lastUpdate = Some(currentUpdate), deltaElapsed, elapsedCycleTime + deltaElapsed)
      case _ => this
