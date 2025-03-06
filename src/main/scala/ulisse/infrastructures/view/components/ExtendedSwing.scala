package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.decorators.ImageEffects.{PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, FontEffect, ShapeEffect}
import ulisse.infrastructures.view.components.styles.Styles

import java.awt.FlowLayout
import javax.swing.JLayeredPane
import scala.swing.*

object ExtendedSwing:

  case class SLayeredPanel private (private val layeredPane: JLayeredPane) extends BorderPanel with EnhancedLook:
    def this() = this(JLayeredPane())
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()

    override def revalidate(): Unit =
      layeredPane.getComponents.foreach(_.setBounds(0, 0, layeredPane.getWidth, layeredPane.getHeight))
      super.revalidate()

  case class SBorderPanel() extends BorderPanel with ShapeEffect

  def createFlowPanel(component: Component*): FlowPanel =
    val panel = SFlowPanel()
    panel.contents ++= component
    panel

  case class SFlowPanel() extends FlowPanel with ShapeEffect with FontEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  case class SBoxPanel(orientation: Orientation.Value) extends BoxPanel(orientation) with ShapeEffect

  case class SPanel() extends Panel with ShapeEffect

  def createPicturePanel(path: String): PicturePanel =
    val panel = PicturePanel()
    panel.picture = path
    panel

  def createSVGPanel(path: String): SVGPanel =
    val panel = SVGPanel()
    panel.svgIcon = path
    panel

  case class PicturePanel() extends Panel with PictureEffect

  case class SVGPanel() extends Panel with SVGEffect

  /** Creates a button for the form, with the given text and rect. */
  def createFormButtonWith(text: String, rect: Styles.Rect): SButton =
    val button = SButton(text)
    button.rect = rect
    button.fontEffect = Styles.whiteFont
    button

  case class SButton(label: String) extends Button(label) with ShapeEffect with FontEffect:
    focusPainted = false
    contentAreaFilled = false

  case class SLabel(label: String) extends Label(label) with ShapeEffect with FontEffect

  case class STextField(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect

  import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
  import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
  extension (panes: Map[ComposedImageLabel, Component])
    /** Returns [[JTabbedPane]] given `panes`, a mapping of tab item [[ComposedImageLabel]] to theirs views. */
    def toTabbedPane: JTabbedPane =
      val tabbedPane = ComposedSwing.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  /** A specific [[STextField]] that accept only numbers. */
  class SNumberField(cols: Int) extends STextField(cols):
    import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
    private val textFieldPadding    = Styles.createPadding(10, 5)
    private val textFieldBackground = Theme.light.background.withAlpha(50)

    rect = rect.withPadding(textFieldPadding)
    rectPalette = rectPalette.withBackground(textFieldBackground)
    import scala.swing.event.ValueChanged
    reactions += {
      case ValueChanged(_) => Swing.onEDT(if (!text.matches("^[0-9]*$")) text = text.filter(_.isDigit))
    }

  import ulisse.infrastructures.view.components.composed.ComposedSwing
  import ulisse.infrastructures.view.common.Themes.withAlpha
  import ulisse.infrastructures.view.components.ExtendedSwing

  /** A themed field label for given `fieldComponent` */
  class SFieldLabel(text: String)(fieldComponent: Component) extends ComposedSwing:
    import ulisse.infrastructures.view.common.Themes.Theme
    private val fieldBackground = Theme.light.background.withAlpha(50)
    private val mainPanel       = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val labelPanel      = ExtendedSwing.SFlowPanel()
    private val label           = ExtendedSwing.SLabel(text)

    fieldComponent.background = fieldBackground
    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += fieldComponent

    override def component[T >: Component]: T = mainPanel
