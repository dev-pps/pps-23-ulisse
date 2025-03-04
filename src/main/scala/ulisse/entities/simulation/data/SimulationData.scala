package ulisse.entities.simulation.data

import ulisse.dsl.comparison.FieldsComparators.{Field, FieldComparator}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Time

trait SimulationData:
  def step: Int
  def secondElapsed: Double
  def simulationEnvironment: RailwayEnvironment
  def increaseStepByOne(): SimulationData
  def increaseSecondElapsedBy(delta: Double): SimulationData
  def reset(): SimulationData

object SimulationData:
  def apply(step: Int, secondElapsed: Double, simulationEnvironment: RailwayEnvironment): SimulationData =
    SimulationDataImpl(step, secondElapsed, simulationEnvironment, simulationEnvironment)
  def empty(): SimulationData = SimulationData(0, 0, RailwayEnvironment.empty())

  extension (simulationData: SimulationData)
    def withEnvironment(environment: RailwayEnvironment): SimulationData =
      SimulationData(simulationData.step, simulationData.secondElapsed, environment)
    def cumulativeDelay: Time =
      Time.secondsToOverflowTime(
        simulationData.simulationEnvironment
          .timetables
          .flatMap(_.currentDelay)
          .map(_.toSeconds).sum
      )

  given FieldComparator[SimulationDataField, SimulationData] with
    def compare(simulationsData: List[SimulationData], ignoredFields: Seq[SimulationDataField]): Boolean =
      val fieldsToCompare = SimulationDataField.values.filterNot(ignoredFields.contains)
      simulationsData match
        case firstSimulationData :: tail => tail.forall: otherSimulationData =>
            fieldsToCompare.forall:
              case SimulationDataField.Step => firstSimulationData.step == otherSimulationData.step
              case SimulationDataField.SecondElapsed =>
                firstSimulationData.secondElapsed == otherSimulationData.secondElapsed
              case SimulationDataField.SimulationEnvironment =>
                firstSimulationData.simulationEnvironment == otherSimulationData.simulationEnvironment
        case _ => false

  enum SimulationDataField extends Field[SimulationDataField, SimulationData]:
    case Step, SecondElapsed, SimulationEnvironment
    def values: Seq[SimulationDataField] = SimulationDataField.values.toSeq

private final case class SimulationDataImpl(
    step: Int,
    secondElapsed: Double,
    initialSimulationEnvironment: RailwayEnvironment,
    simulationEnvironment: RailwayEnvironment
) extends SimulationData:
  override def increaseStepByOne(): SimulationData                    = copy(step = step + 1)
  override def increaseSecondElapsedBy(delta: Double): SimulationData = copy(secondElapsed = secondElapsed + delta)
  override def reset(): SimulationData = SimulationData.empty().withEnvironment(initialSimulationEnvironment)
