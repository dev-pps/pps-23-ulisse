package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Time

trait SimulationData:
  def step: Int
  def secondElapsed: Double
  def simulationEnvironment: RailwayEnvironment
  def simulationEnvironment_=(simulationEnvironment: RailwayEnvironment): SimulationData
  def increaseStepByOne(): SimulationData
  def increaseSecondElapsedBy(delta: Double): SimulationData
  def reset(): SimulationData

object SimulationData:
  def apply(step: Int, secondElapsed: Double, simulationEnvironment: RailwayEnvironment): SimulationData =
    SimulationDataImpl(math.max(0, step), math.max(0, secondElapsed), simulationEnvironment, simulationEnvironment)
  def withEnvironment(environment: RailwayEnvironment): SimulationData =
    SimulationData(0, 0, environment)
  def empty(): SimulationData = withEnvironment(RailwayEnvironment.empty())

  given FieldComparator[SimulationDataField, SimulationData] with
    def fields: Seq[SimulationDataField] = SimulationDataField.values.toSeq
    def _compare(obj: SimulationData, otherObj: SimulationData, field: SimulationDataField): Boolean =
        field match
            case SimulationDataField.Step => obj.step == otherObj.step
            case SimulationDataField.SecondElapsed => obj.secondElapsed == otherObj.secondElapsed
            case SimulationDataField.SimulationEnvironment => obj.simulationEnvironment == otherObj.simulationEnvironment
  
  enum SimulationDataField extends Field[SimulationDataField, SimulationData]:
    case Step, SecondElapsed, SimulationEnvironment
    def values: Seq[SimulationDataField] = SimulationDataField.values.toSeq

private final case class SimulationDataImpl(
    step: Int,
    secondElapsed: Double,
    initialSimulationEnvironment: RailwayEnvironment,
    simulationEnvironment: RailwayEnvironment
) extends SimulationData:
  override def simulationEnvironment_=(simulationEnvironment: RailwayEnvironment): SimulationData =
    copy(simulationEnvironment = simulationEnvironment)
  override def increaseStepByOne(): SimulationData                    = copy(step = step + 1)
  override def increaseSecondElapsedBy(delta: Double): SimulationData = copy(secondElapsed = secondElapsed + delta)
  override def reset(): SimulationData = SimulationData.withEnvironment(initialSimulationEnvironment)
