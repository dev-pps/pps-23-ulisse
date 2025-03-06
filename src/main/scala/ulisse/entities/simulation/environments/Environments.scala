package ulisse.entities.simulation.environments

import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.{EnvironmentElement, TrainAgentEEWrapper}
import ulisse.entities.train.TrainAgents.TrainAgent

object Environments:
  trait Environment:
    type EnvironmentElements <: EnvironmentElement
    def environmentElements: Seq[EnvironmentElements]
  
  trait TrainAgentEnvironment[TAE <: TrainAgentEnvironment[TAE]] extends Environment:
    self: TAE =>
    override type EnvironmentElements <: TrainAgentEEWrapper[?]
    def trainAgents: Seq[TrainAgent]
    def updateTrain(train: TrainAgent): Option[TAE]
    def removeTrain(train: TrainAgent): Option[TAE]
  
  trait EnvironmentsCoordinator[EC <: EnvironmentsCoordinator[EC]]:
    self: EC =>
    def doStep(dt: Int): EC
    def environments: Seq[Environment]
    def agents: Seq[SimulationAgent[?]]
    def perceptionFor[A <: SimulationAgent[A]](agent: A)(using provider: PerceptionProvider[EC, A]): Option[provider.P] =
      provider.perceptionFor(this, agent)
