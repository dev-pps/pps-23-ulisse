package ulisse.infrastructures.view.dashboard

import java.awt.event.{FocusAdapter, FocusEvent}
import java.awt.{Color, Dimension}
import javax.swing.{BorderFactory, Box}
import scala.swing.*

final case class MenuBar() extends BoxPanel(Orientation.Vertical):
  private val buttonMenu = new Button("File"):
    opaque = false
    peer.setContentAreaFilled(false)
    peer.setFocusPainted(false)
    peer.setBorderPainted(false)
    minimumSize = new Dimension(50, 30)
    maximumSize = new Dimension(100, 30)
    preferredSize = new Dimension(100, 30)
    border = BorderFactory.createLineBorder(Color.RED, 2)

  private val projectName = new TextField("Untitled Project"):
    opaque = false
    maximumSize = new Dimension(20, 30)
    border = BorderFactory.createEmptyBorder()
    focusable = false

    peer.addFocusListener(new FocusAdapter:
      override def focusGained(e: FocusEvent): Unit =
        opaque = true
        background = Color.LIGHT_GRAY
      override def focusLost(e: FocusEvent): Unit =
        background = Color.DARK_GRAY
        focusable = false
        opaque = false
    )

    listenTo(mouse.clicks)
    reactions += {
      case _: event.MouseClicked =>
        focusable = true
        requestFocusInWindow()
    }

  private val gluePanel = Swing.HGlue
  gluePanel.listenTo(mouse.clicks)
  gluePanel.reactions += {
    case _: event.MouseClicked => projectName.peer.transferFocus() // Transfer focus when clicked
  }

  background = Color.DARK_GRAY
  contents += Swing.VStrut(10)
  contents += new BoxPanel(Orientation.Horizontal):
    background = Color.DARK_GRAY
    contents += Swing.HStrut(10)
    contents += buttonMenu
    contents += Swing.HStrut(10)
    contents += projectName
    contents += gluePanel
  contents += Swing.VStrut(10)
