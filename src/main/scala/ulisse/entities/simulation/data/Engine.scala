package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}

trait Engine:
  def running: Boolean
  def running_=(running: Boolean): Engine
  def configuration: EngineConfiguration
  def configuration_=(configuration: EngineConfiguration): Engine
  def state: EngineState
  def state_=(state: EngineState): Engine
  def reset(): Engine

object Engine:
  def apply(running: Boolean, configuration: EngineConfiguration, state: EngineState): Engine =
    EngineImpl(running, configuration, state)

  def empty(): Engine = Engine.emptyWithConfiguration(EngineConfiguration.empty())

  def emptyWithConfiguration(configuration: EngineConfiguration): Engine =
    Engine(false, configuration, EngineState.empty())

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
