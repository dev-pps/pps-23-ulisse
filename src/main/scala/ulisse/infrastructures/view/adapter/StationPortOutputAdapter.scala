package ulisse.infrastructures.view.adapter

import ulisse.applications.ports.StationPort
import ulisse.infrastructures.view.station.StationEditorView

final case class StationPortOutputAdapter(stationEditorView: StationEditorView)
    extends StationPort.Output:
  override def show(): Unit = stationEditorView.visible = true
