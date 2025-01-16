package infrastructures.ui.adapter

import applications.ports.StationPort
import infrastructures.ui.station.StationEditorView

final case class StationPortOutputAdapter(stationEditorView: StationEditorView)
    extends StationPort.Outbound:
  override def show(): Unit = stationEditorView.visible = true
