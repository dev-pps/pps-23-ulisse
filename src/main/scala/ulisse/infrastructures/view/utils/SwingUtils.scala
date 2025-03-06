package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.components.ExtendedSwing.{SFlowPanel, STextField}
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.*

object SwingUtils:

  val nameFont  = new Font("Arial", java.awt.Font.BOLD, 18)
  val labelFont = new Font("Arial", java.awt.Font.BOLD, 14)
  val valueFont = new Font("Arial", java.awt.Font.PLAIN, 14)

  extension (s: Option[String])
    def defaultString: String = s.getOrElse("N/A")

  extension (i: Option[Int])
    def defaultIntString: String = i.map(_.toString).defaultString

  extension (text: String)
    def headerLabel: Label =
      new Label(text) {
        font = labelFont
      }

    def valueLabel: Label =
      new Label(text) {
        font = valueFont
      }

  /** Open component `c` inside a [[MainFrame]]. */
  extension (c: Component)
    def showPreview(): MainFrame =
      new MainFrame() {
        title = "timetable preview"
        val mainPanel: SFlowPanel = SFlowPanel()
        mainPanel.contents += c
        contents = mainPanel
        visible = true
      }
