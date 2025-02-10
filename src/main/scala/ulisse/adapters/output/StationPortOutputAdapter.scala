package ulisse.adapters.output

import ulisse.adapters.input.StationEditorAdapter
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

final case class StationPortOutputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[C]](
    stationEditorController: StationEditorAdapter[N, C, S]
) extends StationPorts.Output
