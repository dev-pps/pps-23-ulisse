package ulisse.infrastructures.view.adapter

import ulisse.applications.ports.StationPorts
import ulisse.infrastructures.view.station.StationEditorView

final case class StationPortOutputAdapter(stationEditorView: StationEditorView)
    extends StationPorts.Output:
  override def show(): Unit = stationEditorView.visible = true
