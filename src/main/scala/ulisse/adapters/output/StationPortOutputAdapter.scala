package ulisse.adapters.output

import ulisse.adapters.input.StationEditorController
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationPortOutputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    stationEditorController: StationEditorController[N, C, S]
) extends StationPorts.Output
