package ulisse.entities.simulation

object Actions:
  sealed trait SimulationAction
  final case class MoveBy(distance: Double) extends SimulationAction
