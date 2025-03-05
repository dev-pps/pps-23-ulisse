package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.simulation.data.{Engine, SimulationData}

import java.sql.Time
import scala.concurrent.Future

/** Adapter for the SimulationPorts.Input port. */
final case class SimulationPageAdapter(private val simulationPort: SimulationPorts.Input):
  export simulationPort.*
