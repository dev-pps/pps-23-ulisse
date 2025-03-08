package ulisse.entities.simulation.data
import ulisse.utils.OptionUtils.when

/** Configuration for simulation engine */
trait EngineConfiguration:
  /** delta time to run simulation step */
  def stepSize: Int

  /** cycles per second for rendering the simulation */
  def cyclesPerSecond: Option[Int]

/** Factory for [[EngineConfiguration]] instances */
object EngineConfiguration:
  /** Default step size */
  val defaultStepSize: Int = 1

  /** Default cycles per second */
  val defaultCyclesPerSecond: Int = 1

  /** Create a new Engine configuration, allowing only non-negative stepSize and positive cyclesPerSecond, otherwise it replaces the wrong values respectively with default and None values */
  def apply(stepSize: Int, cyclesPerSecond: Option[Int]): EngineConfiguration =
    EngineConfigurationImpl(math.max(0, stepSize), cyclesPerSecond.filter(_ > 0))

  /** Create a new Engine configuration with default stepSize and provided cyclesPerSecond, if the value is non-positive it's replaced with None */
  def withCps(cps: Int): EngineConfiguration = EngineConfiguration(defaultStepSize, Some(cps))

  /** Create a new Engine configuration with provided values if stepSize is non-negative and cyclesPerSecond is positive */
  def createCheckedConfiguration(stepSize: Int, cyclesPerSecond: Option[Int]): Option[EngineConfiguration] =
    EngineConfiguration(stepSize, cyclesPerSecond) when stepSize >= 0 && cyclesPerSecond.exists(_ > 0)

  /** Create a new Engine configuration with default stepSize and no cyclesPerSecond */
  def defaultBatch(): EngineConfiguration = EngineConfiguration(defaultStepSize, None)

  /** Create a new Engine configuration with default stepSize and default cyclesPerSecond */
  def defaultTimed(): EngineConfiguration = EngineConfiguration(defaultStepSize, Some(defaultCyclesPerSecond))

  /** Create a new Engine configuration with no stepSize and no cyclesPerSecond */
  def empty(): EngineConfiguration = EngineConfigurationImpl(0, None)
  private final case class EngineConfigurationImpl(stepSize: Int, cyclesPerSecond: Option[Int])
      extends EngineConfiguration
