package ulisse.entities.simulation.environments

import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.{EnvironmentElement, TrainAgentEEWrapper}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen

object Environments:
  trait Environment[EnvironmentElements <: EnvironmentElement]:
    def environmentElements: Seq[EnvironmentElements]

  trait TrainAgentEnvironment[TAE <: TrainAgentEnvironment[TAE, EE], EE <: TrainAgentEEWrapper[EE]]
      extends Environment[EE]:
    self: TAE =>
    def trainAgents: Seq[TrainAgent]                = environmentElements.collectTrains
    def updateTrain(train: TrainAgent): Option[TAE] = doOperationOn(train, _.updateTrain(train))
    def removeTrain(train: TrainAgent): Option[TAE] = doOperationOn(train, _.removeTrain(train))
    private def doOperationOn(
        train: TrainAgent,
        operation: EE => Option[EE]
    ): Option[TAE] =
      for
        ee        <- environmentElements.find(_.contains(train))
        updatedEE <- operation(ee)
      yield constructor(environmentElements.updateWhen(_ == ee)(_ => updatedEE))
    protected def constructor(environmentElements: Seq[EE]): TAE
  trait EnvironmentsCoordinator[EC <: EnvironmentsCoordinator[EC]]:
    self: EC =>
    def doStep(dt: Int): EC
    def environments: Seq[Environment[?]]
    def agents: Seq[SimulationAgent[?]]
    def perceptionFor[SA <: SimulationAgent[SA]](simulationAgent: SA)(using
        provider: PerceptionProvider[EC, SA]
    ): Option[provider.P] =
      provider.perceptionFor(this, simulationAgent)
