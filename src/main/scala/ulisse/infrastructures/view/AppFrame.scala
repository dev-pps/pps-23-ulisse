package ulisse.infrastructures.view

import scala.swing.*

final case class AppFrame() extends MainFrame:
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  pack()
  centerOnScreen()
////  private val model: Manger = Manger()
////  private lazy val stationEditorController: StationEditorController =
////    StationEditorController(stationEditorView, model)
////  private val stationEditorView: StationEditorView =
////    StationEditorView(stationEditorController)
////  title = "Station Editor"
////  minimumSize = new Dimension(400, 300)
////  preferredSize = new Dimension(800, 600)
////  contents = stationEditorView
////  pack()
////  centerOnScreen()
//
