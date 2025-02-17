package ulisse.entities.simulation

import ulisse.entities.simulation.Agents.{SimulationAgent, TrainAgent}
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.Station

object Environments:

  trait SimulationEnvironment:
    def doStep(dt: Int): SimulationEnvironment
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
      def doStep(dt: Int): SimulationEnvironment =
        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
        stepActions.foldLeft(this.copy(agents = Seq())) {
          case (env, (agent: TrainAgent, Actions.MoveBy(d))) =>
            env.copy(agents = agents ++ Seq(agent.updateTravelDistance(d)))
          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
        }
      def stations_=(newStations: Seq[Station]): SimulationEnvironment =
        copy(stations = newStations)
      def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment =
        copy(agents = newAgents)
