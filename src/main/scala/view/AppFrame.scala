package view

import scala.swing.*

final case class AppFrame() extends MainFrame:
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  contents = StationEditorPage()
  pack()
  centerOnScreen()
