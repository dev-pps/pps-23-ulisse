package ulisse.entities.simulation

import ulisse.entities.station.Station

object Environments:
  final case class SimulationEnvironment(
      stationMap: Seq[Station[?]]
  )
