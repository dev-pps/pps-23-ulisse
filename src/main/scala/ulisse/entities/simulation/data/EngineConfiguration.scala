package ulisse.entities.simulation.data

trait EngineConfiguration:
  def stepSize: Int
  def cyclesPerSecond: Option[Int] // TODO check positive (non-negative?)

object EngineConfiguration:
  def apply(stepSize: Int, cyclesPerSecond: Option[Int]): EngineConfiguration =
    EngineConfigurationImpl(stepSize, cyclesPerSecond)
  def empty(): EngineConfiguration                     = EngineConfigurationImpl(0, None)
  def withStepSize(stepSize: Int): EngineConfiguration = EngineConfigurationImpl(stepSize, None)
  def withCyclesPerSecond(cyclesPerSecond: Option[Int]): EngineConfiguration =
    EngineConfigurationImpl(0, cyclesPerSecond)
  private final case class EngineConfigurationImpl(stepSize: Int, cyclesPerSecond: Option[Int])
      extends EngineConfiguration
