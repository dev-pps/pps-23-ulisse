package ulisse.entities.simulation.agents

import ulisse.entities.simulation.environments.Environments.EnvironmentsCoordinator

/** Contains the Perceptions objects used in the simulation */
object Perceptions:
  /** Basic PerceptionData trait */
  trait PerceptionData

  /** Basic Perception trait */
  trait Perception[PD <: PerceptionData]:
    /** Data of the perception */
    def perceptionData: PD

  /** PerceptionProvider trait */
  trait PerceptionProvider[EC <: EnvironmentsCoordinator[EC], SA <: SimulationAgent[SA]]:
    /** Provided Perception type */
    type P <: Perception[?]

    /** Provides a perception for the agent given the environment */
    def perceptionFor(environment: EC, agent: SA): Option[P]
