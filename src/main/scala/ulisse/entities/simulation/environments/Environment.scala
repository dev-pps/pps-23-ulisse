package ulisse.entities.simulation.environments

import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.EnvironmentElement

trait Environment[E <: Environment[E]]:
  self: E =>
  def doStep(dt: Int): E
  def environmentElements: List[EnvironmentElement]
  def agents: List[SimulationAgent[?]]
  def perceptionFor[A <: SimulationAgent[A]](agent: A)(using provider: PerceptionProvider[E, A]): Option[provider.P] =
    provider.perceptionFor(this, agent)
