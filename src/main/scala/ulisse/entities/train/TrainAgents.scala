package ulisse.entities.train

import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.MotionDatas.{emptyMotionData, MotionData}
import ulisse.entities.train.TrainAgentPerceptions.*
import ulisse.entities.train.TrainAgentStates.StateBehavior

/** Object that group [[TrainAgent]] entity and [[TrainAgentInfo]]. */
object TrainAgents:

  /** Train agent infos. */
  trait TrainAgentInfo:
    /** Return train agent. */
    def train: TrainAgent

    /** Returns train related DynamicTimetable. */
    def timetables: Seq[DynamicTimetable]

  /** Companion object of trait [[TrainAgentInfo]] */
  object TrainAgentInfo:
    def apply(train: TrainAgent, timetables: Seq[DynamicTimetable]): TrainAgentInfo =
      TrainAgentInfoImpl(train, timetables.filter(_.train == train))
    private final case class TrainAgentInfoImpl(train: TrainAgent, timetables: Seq[DynamicTimetable])
        extends TrainAgentInfo

  /** Train Agent entity is a [[SimulationAgent]] for a given Train.
    *
    * Exposes methods to get current motion data, distance travelled (and methods to reset or apply some delta) and state.
    */
  trait TrainAgent extends Train with SimulationAgent[TrainAgent]:
    override type EC = RailwayEnvironment

    /** Returns current agent state. */
    def state: TrainAgentStates.StateBehavior

    /** Returns current motion data. */
    def motionData: MotionDatas.MotionData

    /** Returns distance travelled until the last step. */
    def distanceTravelled: Double

    /** Return updated TrainAgent with resetted distance travelled */
    def resetDistanceTravelled: TrainAgent

    /** Returns updates TrainAgent with some `distanceDelta` to distance travelled */
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent

  /** Companion object of trait [[TrainAgent]] */
  object TrainAgent:
    /** Creates a [[TrainAgent]] for a given `train` with default [[Stopped]] state and no distance travelled. */
    def apply(train: Train): TrainAgent =
      TrainAgentImpl(train, TrainAgentStates.Stopped(emptyMotionData))

    /** Returns new TrainAgent with some initial `state` and `train`. */
    def withInitialState(train: Train, state: StateBehavior): TrainAgent =
      TrainAgentImpl(train, state)

    private final case class TrainAgentImpl(train: Train, state: TrainAgentStates.StateBehavior)
        extends TrainAgent:
      export train.*

      override def motionData: MotionData             = state.motionData
      override def resetDistanceTravelled: TrainAgent = TrainAgentImpl(train, state.reset())
      override def updateDistanceTravelled(distanceDelta: Double): TrainAgent =
        TrainAgentImpl(train, state.withOffsetDistance(distanceDelta))

      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): TrainAgent =
        import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProviders.given
        val perception: Option[TrainAgentPerception[?]] = simulationEnvironment.perceptionFor[TrainAgent](this)
        println(perception)
        copy(state = state.next(this, dt, perception))

      override def distanceTravelled: Double = state.motionData.distanceTravelled
