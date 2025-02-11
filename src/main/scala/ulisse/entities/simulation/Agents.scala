package ulisse.entities.simulation

object Agents:
  final case class SimulationAgent(timeStep: Double):
    /* dt is time elapsed between each frame so
         dt * timeUpdatePerSecond is the fraction of
         the movement that has to be done
         if dt > 1 then the simulation could break
     */
    def update(): SimulationAgent
