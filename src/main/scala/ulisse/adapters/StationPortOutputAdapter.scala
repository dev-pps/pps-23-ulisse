package ulisse.adapters

import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.infrastructures.view.station.StationEditorController

final case class StationPortOutputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    stationEditorController: StationEditorController[N, C, S]
) extends StationPorts.Output
