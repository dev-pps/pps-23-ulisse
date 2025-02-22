package ulisse.entities.simulation

import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.station.Station

object Environments:
  trait SimulationEnvironment:
    def stations: Seq[Station]
    def agents: Seq[SimulationAgent]
    def stations_=(newStations: Seq[Station]): SimulationEnvironment
    def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment

  object SimulationEnvironment:
    def apply(stations: Seq[Station], agents: Seq[SimulationAgent]): SimulationEnvironment =
      SimulationEnvironmentImpl(stations, agents)
    def empty(): SimulationEnvironment =
      apply(Seq[Station](), Seq[SimulationAgent]())

    private final case class SimulationEnvironmentImpl(
        stations: Seq[Station],
        agents: Seq[SimulationAgent]
    ) extends SimulationEnvironment:
      def stations_=(newStations: Seq[Station]): SimulationEnvironment =
        copy(stations = newStations)
      def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment =
        copy(agents = newAgents)
