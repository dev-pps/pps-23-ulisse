package ulisse.entities.simulation.data

trait EngineState:
  def lastUpdate: Option[Double]
  def lastUpdate_=(lastUpdate: Option[Double]): EngineState
  def lastDelta: Double
  def elapsedCycleTime: Double
  def updateElapsedCycleTime(delta: Double): EngineState

object EngineState:
  def apply(lastUpdate: Option[Double], lastDelta: Double, elapsedCycleTime: Double): EngineState =
    EngineStateImpl(lastUpdate, lastDelta, elapsedCycleTime)
  def empty(): EngineState = EngineState(None, 0, 0)
  private final case class EngineStateImpl(
      lastUpdate: Option[Double],
      lastDelta: Double,
      elapsedCycleTime: Double
  ) extends EngineState:
    override def lastUpdate_=(lastUpdate: Option[Double]): EngineState = copy(lastUpdate = lastUpdate)
    override def updateElapsedCycleTime(delta: Double): EngineState = copy(elapsedCycleTime = elapsedCycleTime + delta)
