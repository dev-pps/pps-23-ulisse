package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}

/** Engine for simulation. */
trait Engine:
  /** Running indication. */
  def running: Boolean

  /** Modify running state. */
  def running_=(running: Boolean): Engine

  /** Configuration of the engine. */
  def configuration: EngineConfiguration

  /** Modify configuration. */
  def configuration_=(configuration: EngineConfiguration): Engine

  /** State of the engine. */
  def state: EngineState

  /** Modify state. */
  def state_=(state: EngineState): Engine

  /** Reset the engine state. */
  def reset(): Engine

/** Factory for [[Engine]] instances. */
object Engine:
  /** Create a new engine. */
  def apply(running: Boolean, configuration: EngineConfiguration, state: EngineState): Engine =
    EngineImpl(running, configuration, state)

  /** Create an engine with the given configuration. */
  def emptyWithConfiguration(configuration: EngineConfiguration): Engine =
    Engine(false, configuration, EngineState.empty())

  /** Create an engine with empty configuration. */
  def empty(): Engine = Engine.emptyWithConfiguration(EngineConfiguration.empty())

  /** Create a FiledComparator for Engine. */
  given FieldComparator[EngineField, Engine] with
    def fields: Seq[EngineField] = EngineField.values.toSeq
    def _compare(firstEngine: Engine, otherEngine: Engine, field: EngineField): Boolean =
      field match
        case EngineField.Running       => firstEngine.running == otherEngine.running
        case EngineField.Configuration => firstEngine.configuration == otherEngine.configuration
        case EngineField.State         => firstEngine.state == otherEngine.state

  /** Field of an engine. */
  enum EngineField extends Field[EngineField, Engine]:
    case Running, Configuration, State
    def values: Seq[EngineField] = EngineField.values.toSeq

  private final case class EngineImpl(
      running: Boolean,
      configuration: EngineConfiguration,
      state: EngineState
  ) extends Engine:
    override def running_=(running: Boolean): Engine                         = copy(running = running)
    override def configuration_=(configuration: EngineConfiguration): Engine = copy(configuration = configuration)
    override def state_=(state: EngineState): Engine                         = copy(state = state)
    override def reset(): Engine = Engine(false, configuration, EngineState.empty())
