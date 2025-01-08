package views

import controllers.station.StationEditorController
import model.Model
import views.station.StationEditorView

import scala.swing.*

final case class AppFrame() extends MainFrame:
  private val model: Model = Model()
  private lazy val stationEditorController: StationEditorController =
    StationEditorController(stationEditorView, model)
  private val stationEditorView: StationEditorView =
    StationEditorView(stationEditorController)
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  contents = stationEditorView
  pack()
  centerOnScreen()
