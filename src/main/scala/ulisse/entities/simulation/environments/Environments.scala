package ulisse.entities.simulation.environments

import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.{EnvironmentElement, TrainAgentEEWrapper}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.swapWhenEq

/** Environments for the simulation. */
object Environments:
  /** Base Environment. */
  trait Environment[EnvironmentElements <: EnvironmentElement]:
    /** The list of contained environment elements. */
    def environmentElements: Seq[EnvironmentElements]

  /** Train Agent Environment. */
  trait TrainAgentEnvironment[TAE <: TrainAgentEnvironment[TAE, EE], EE <: TrainAgentEEWrapper[EE]]
      extends Environment[EE]:
    self: TAE =>

    /** The list of trains in the environment. */
    def trainAgents: Seq[TrainAgent] = environmentElements.collectTrains

    /** Update the train in the environment. */
    def updateTrain(train: TrainAgent): Option[TAE] = doOperationOn(train, _.updateTrain(train))

    /** Remove the train from the environment. */
    def removeTrain(train: TrainAgent): Option[TAE] = doOperationOn(train, _.removeTrain(train))

    /** Constructor to update the environment. */
    protected def constructor(environmentElements: Seq[EE]): TAE

    private def doOperationOn(
        train: TrainAgent,
        operation: EE => Option[EE]
    ): Option[TAE] =
      for
        ee        <- environmentElements.find(_.contains(train))
        updatedEE <- operation(ee)
      yield constructor(environmentElements.swapWhenEq(ee)(updatedEE))

  /** Environments Coordinator. */
  trait EnvironmentsCoordinator[EC <: EnvironmentsCoordinator[EC]]:
    self: EC =>

    /** Make a simulation step with the provided dt. */
    def doStep(dt: Int): EC

    /** The list of environments in the coordinator. */
    def environments: Seq[Environment[?]]

    /** The list of agents in the coordinator. */
    def agents: Seq[SimulationAgent[?]]

    /** Return a perception to the agent based on the current state. */
    def perceptionFor[SA <: SimulationAgent[SA]](simulationAgent: SA)(using
        provider: PerceptionProvider[EC, SA]
    ): Option[provider.P] =
      provider.perceptionFor(this, simulationAgent)
