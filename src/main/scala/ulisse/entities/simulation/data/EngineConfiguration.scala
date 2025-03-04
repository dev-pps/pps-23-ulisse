package ulisse.entities.simulation.data
import ulisse.utils.OptionUtils.when

trait EngineConfiguration:
  def stepSize: Int
  def cyclesPerSecond: Option[Int]

object EngineConfiguration:
  val defaultStepSize: Int        = 1
  val defaultCyclesPerSecond: Int = 1
  def apply(stepSize: Int, cyclesPerSecond: Option[Int]): EngineConfiguration =
    EngineConfigurationImpl(math.max(0, stepSize), cyclesPerSecond.filter(_ > 0))
  def withCps(cps: Int): EngineConfiguration = EngineConfiguration(defaultStepSize, Some(cps))
  def createCheckedConfiguration(stepSize: Int, cyclesPerSecond: Option[Int]): Option[EngineConfiguration] =
    EngineConfiguration(stepSize, cyclesPerSecond) when stepSize >= 0 && cyclesPerSecond.exists(_ > 0)
  def defaultBatch(): EngineConfiguration = EngineConfiguration(defaultStepSize, None)
  def defaultTimed(): EngineConfiguration = EngineConfiguration(defaultStepSize, Some(defaultCyclesPerSecond))
  def empty(): EngineConfiguration        = EngineConfigurationImpl(0, None)
  private final case class EngineConfigurationImpl(stepSize: Int, cyclesPerSecond: Option[Int])
      extends EngineConfiguration
