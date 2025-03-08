package ulisse.entities.simulation.data

import ulisse.dsl.FieldsComparators.{Field, FieldComparator}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Milliseconds

/** Simulation data. */
trait SimulationData:
  /** Step of the simulation. */
  def step: Int

  /** Milliseconds elapsed in the simulation. */
  def millisecondsElapsed: Milliseconds

  /** Environment of the simulation. */
  def simulationEnvironment: RailwayEnvironment

  /** Set the simulation environment. */
  def simulationEnvironment_=(simulationEnvironment: RailwayEnvironment): SimulationData

  /** Increase the step by one. */
  def increaseStepByOne(): SimulationData

  /** Increase the milliseconds elapsed by delta. */
  def increaseMillisecondsElapsedBy(delta: Long): SimulationData

  /** Reset the simulation data and set the environment as the initial one. */
  def reset(): SimulationData

/** Factory for [[SimulationData]] instances. */
object SimulationData:
  /** Create a new Simulation data. */
  def apply(step: Int, milliseconds: Milliseconds, simulationEnvironment: RailwayEnvironment): SimulationData =
    SimulationDataImpl(math.max(0, step), math.max(0, milliseconds), simulationEnvironment, simulationEnvironment)

  /** Create a new Simulation data with initial state and the given environment. */
  def withEnvironment(environment: RailwayEnvironment): SimulationData =
    SimulationData(0, 0, environment)

  /** Create a new empty Simulation data with initial state and an empty environment. */
  def empty(): SimulationData = withEnvironment(RailwayEnvironment.empty())

  /** Create a FieldComparator for SimulationData. */
  given FieldComparator[SimulationDataField, SimulationData] with
    def fields: Seq[SimulationDataField] = SimulationDataField.values.toSeq
    def _compare(obj: SimulationData, otherObj: SimulationData, field: SimulationDataField): Boolean =
      field match
        case SimulationDataField.Step                  => obj.step == otherObj.step
        case SimulationDataField.MillisecondElapsed    => obj.millisecondsElapsed == otherObj.millisecondsElapsed
        case SimulationDataField.SimulationEnvironment => obj.simulationEnvironment == otherObj.simulationEnvironment

  /** Field of a simulation data. */
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
