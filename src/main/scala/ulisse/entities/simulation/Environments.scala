package ulisse.entities.simulation

import ulisse.entities.station.StationEnvironmentElement

object Environments:

  trait SimulationEnvironment:
    def doStep(dt: Int): SimulationEnvironment
    def stations: Seq[StationEnvironmentElement]
    def agents: Seq[SimulationAgent]
    def stations_=(newStations: Seq[StationEnvironmentElement]): SimulationEnvironment
    def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment

  object SimulationEnvironment:
    def apply(stations: Seq[StationEnvironmentElement], agents: Seq[SimulationAgent]): SimulationEnvironment =
      SimulationEnvironmentImpl(stations, agents)
    def empty(): SimulationEnvironment =
      apply(Seq[StationEnvironmentElement](), Seq[SimulationAgent]())

    private final case class SimulationEnvironmentImpl(
        stations: Seq[StationEnvironmentElement],
        agents: Seq[SimulationAgent]
    ) extends SimulationEnvironment:
      def doStep(dt: Int): SimulationEnvironment =
        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
        stepActions.foldLeft(this.copy(agents = Seq())) {
          case (env, (agent: TrainAgent, Actions.MoveBy(d))) =>
            env.copy(agents = agents ++ Seq(agent.updateTravelDistance(d)))
          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
        }
      def stations_=(newStations: Seq[StationEnvironmentElement]): SimulationEnvironment =
        copy(stations = newStations)
      def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment =
        copy(agents = newAgents)
