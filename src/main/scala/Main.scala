import scala.swing.*
import scala.swing.event.*

class EditMenu extends GridBagPanel {
  private val c = new Constraints
  c.anchor = GridBagPanel.Anchor.Center
  c.gridx = 0

  c.gridy = 0
  c.weighty = 1
  layout(Swing.VGlue) = c

  c.gridy = 1
  c.weighty = 0.1
  layout(new Label("Select a Station")) = c

  c.gridy = 2
  c.weighty = 0.1
  layout(new Label("or")) = c

  c.gridy = 3
  c.weighty = 0.1
  layout(new Button {
    text = "Create"
    reactions += {
      case ButtonClicked(_) =>
        println("Create New Station")
    }
  }) = c

  c.gridy = 4
  c.weighty = 1.0
  layout(Swing.VGlue) = c
}

class AppFrame extends MainFrame:
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  contents = new GridBagPanel:
    val c = new Constraints
    c.fill = GridBagPanel.Fill.Both
    c.gridx = 0
    c.gridy = 0
    c.weighty = 1
    c.weightx = 0.7
    layout(new Panel {
      background = java.awt.Color.RED
    }) = c
    c.gridx = 1
    c.weightx = 0.3
    layout(EditMenu()) = c
  pack()
  centerOnScreen()

@main def run(): Unit =
  AppFrame().open()
