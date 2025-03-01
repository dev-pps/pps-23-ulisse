package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.ExtendedSwing.{SFlowPanel, STextField}
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.event.ValueChanged
import scala.swing.*

object SwingUtils:

  val nameFont  = new Font("Arial", java.awt.Font.BOLD, 18)
  val labelFont = new Font("Arial", java.awt.Font.BOLD, 14)
  val valueFont = new Font("Arial", java.awt.Font.PLAIN, 14)

  extension (s: Option[String])
    implicit def defaultString: String =
      s.getOrElse("N/A")

  extension (i: Option[Int])
    implicit def defaultIntString: String =
      i match
        case Some(v) => v.toString
        case None    => "N/A"

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

  extension (panes: Map[ComposedImageLabel, Component])
    /** Returns [[JTabbedPane]] given `panes`, a mapping of tab item [[ComposedImageLabel]] to theirs views. */
    def toTabbedPane: JTabbedPane =
      val tabbedPane = ComposedSwing.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  /** A specific [[STextField]] that accept only numbers. */
  class SNumberField(cols: Int) extends STextField(cols):
    import ulisse.infrastructures.view.common.Themes.withAlpha
    private val textFieldPadding    = Styles.createPadding(10, 5)
    private val textFieldBackground = Theme.light.background.withAlpha(50)

    rect = rect.withPadding(textFieldPadding)
    rectPalette = rectPalette.withBackground(textFieldBackground)
    reactions += {
      case ValueChanged(_) => Swing.onEDT(if (!text.matches("^[0-9]*$")) text = text.filter(_.isDigit))
    }

  import ulisse.infrastructures.view.components.composed.ComposedSwing
  import ulisse.infrastructures.view.common.Themes.withAlpha
  import ulisse.infrastructures.view.components.ExtendedSwing

  /** A themed field label for given `fieldComponent` */
  class SFieldLabel(text: String)(fieldComponent: Component) extends ComposedSwing:
    private val fieldBackground = Theme.light.background.withAlpha(50)
    private val mainPanel       = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val labelPanel      = ExtendedSwing.SFlowPanel()
    private val label           = ExtendedSwing.SLabel(text)

    fieldComponent.background = fieldBackground
    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += fieldComponent

    override def component[T >: Component]: T = mainPanel
