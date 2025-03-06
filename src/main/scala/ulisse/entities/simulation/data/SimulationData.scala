package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Milliseconds

trait SimulationData:
  def step: Int
  def millisecondsElapsed: Milliseconds
  def simulationEnvironment: RailwayEnvironment
  def simulationEnvironment_=(simulationEnvironment: RailwayEnvironment): SimulationData
  def increaseStepByOne(): SimulationData
  def increaseMillisecondsElapsedBy(delta: Long): SimulationData
  def reset(): SimulationData

object SimulationData:
  def apply(step: Int, milliseconds: Milliseconds, simulationEnvironment: RailwayEnvironment): SimulationData =
    SimulationDataImpl(math.max(0, step), math.max(0, milliseconds), simulationEnvironment, simulationEnvironment)
  def withEnvironment(environment: RailwayEnvironment): SimulationData =
    SimulationData(0, 0, environment)
  def empty(): SimulationData = withEnvironment(RailwayEnvironment.empty())

  given FieldComparator[SimulationDataField, SimulationData] with
    def fields: Seq[SimulationDataField] = SimulationDataField.values.toSeq
    def _compare(obj: SimulationData, otherObj: SimulationData, field: SimulationDataField): Boolean =
      field match
        case SimulationDataField.Step                  => obj.step == otherObj.step
        case SimulationDataField.MillisecondElapsed    => obj.millisecondsElapsed == otherObj.millisecondsElapsed
        case SimulationDataField.SimulationEnvironment => obj.simulationEnvironment == otherObj.simulationEnvironment

  enum SimulationDataField extends Field[SimulationDataField, SimulationData]:
    case Step, MillisecondElapsed, SimulationEnvironment
    def values: Seq[SimulationDataField] = SimulationDataField.values.toSeq

private final case class SimulationDataImpl(
    step: Int,
    millisecondsElapsed: Milliseconds,
    initialSimulationEnvironment: RailwayEnvironment,
    simulationEnvironment: RailwayEnvironment
) extends SimulationData:
  override def simulationEnvironment_=(simulationEnvironment: RailwayEnvironment): SimulationData =
    copy(simulationEnvironment = simulationEnvironment)
  override def increaseStepByOne(): SimulationData = copy(step = step + 1)
  override def increaseMillisecondsElapsedBy(delta: Long): SimulationData =
    copy(millisecondsElapsed = millisecondsElapsed + delta)
  override def reset(): SimulationData = SimulationData.withEnvironment(initialSimulationEnvironment)
