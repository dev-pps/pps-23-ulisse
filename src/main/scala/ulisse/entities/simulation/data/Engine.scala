package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}

/** Engine for simulation */
trait Engine:
  /** running indication */
  def running: Boolean

  /** modify running state */
  def running_=(running: Boolean): Engine

  /** configuration of the engine */
  def configuration: EngineConfiguration

  /** modify configuration */
  def configuration_=(configuration: EngineConfiguration): Engine

  /** state of the engine */
  def state: EngineState

  /** modify state */
  def state_=(state: EngineState): Engine

  /** reset the engine state */
  def reset(): Engine

/** Factory for [[Engine]] instances. */
object Engine:
  /** Create a new engine */
  def apply(running: Boolean, configuration: EngineConfiguration, state: EngineState): Engine =
    EngineImpl(running, configuration, state)

  /** Create an engine with the given configuration */
  def emptyWithConfiguration(configuration: EngineConfiguration): Engine =
    Engine(false, configuration, EngineState.empty())

  /** Create an engine with empty configuration */
  def empty(): Engine = Engine.emptyWithConfiguration(EngineConfiguration.empty())

  // TODO
  given FieldComparator[EngineStateField, Engine] with
    def fields: Seq[EngineStateField] = EngineStateField.values.toSeq
    def _compare(firstEngine: Engine, otherEngine: Engine, field: EngineStateField): Boolean =
      field match
        case EngineStateField.Running => firstEngine.running == otherEngine.running
        case EngineStateField.CyclesPerSecond =>
          firstEngine.configuration.cyclesPerSecond == otherEngine.configuration.cyclesPerSecond
        case EngineStateField.LastUpdate => firstEngine.state.lastUpdate == otherEngine.state.lastUpdate
        case EngineStateField.LastDelta  => firstEngine.state.lastDelta == otherEngine.state.lastDelta
        case EngineStateField.ElapsedCycleTime =>
          firstEngine.state.elapsedCycleTime == otherEngine.state.elapsedCycleTime

  enum EngineStateField extends Field[EngineStateField, Engine]:
    case Running, CyclesPerSecond, LastUpdate, LastDelta, ElapsedCycleTime
    def values: Seq[EngineStateField] = EngineStateField.values.toSeq

  private final case class EngineImpl(
      running: Boolean,
      configuration: EngineConfiguration,
      state: EngineState
  ) extends Engine:
    def reset(): Engine                              = Engine(false, configuration, EngineState.empty())
    override def running_=(running: Boolean): Engine = copy(running = running)
    override def configuration_=(configuration: EngineConfiguration): Engine = copy(configuration = configuration)
    override def state_=(state: EngineState): Engine                         = copy(state = state)
