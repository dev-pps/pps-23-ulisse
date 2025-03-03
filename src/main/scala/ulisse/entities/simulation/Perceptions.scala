package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.Environment

object Perceptions:
  trait PerceptionData

  trait Perception[PD <: PerceptionData]:
    def perceptionData: PD

  trait PerceptionProvider[E <: Environment[E], SA <: SimulationAgent[SA]]:
    type P <: Perception[?]
    def perceptionFor(environment: E, agent: SA): Option[P]
