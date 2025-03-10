package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
import ulisse.infrastructures.view.components.composed.ComposedSwing.STabbedPane
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.decorators.ImageEffects.{PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{EnhancedLook, FontEffect, ShapeEffect}
import ulisse.infrastructures.view.components.styles.Styles

import java.awt.FlowLayout
import javax.swing.{JLayeredPane, JTextArea}
import scala.swing.*

/** Extended Swing components. */
@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
object ExtendedSwing:

  /** Creates a layered panel. */
  case class SLayeredPanel private (private val layeredPane: JLayeredPane) extends BorderPanel with EnhancedLook:
    def this() = this(JLayeredPane())
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    /** Adds a component to the layered panel. */
    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()

    override def revalidate(): Unit =
      layeredPane.getComponents.foreach(_.setBounds(0, 0, layeredPane.getWidth, layeredPane.getHeight))
      super.revalidate()

  /** Creates a border panel with shape effect. */
  case class SBorderPanel() extends BorderPanel with ShapeEffect

  /** Creates a flow panel with shape effect and the given components. */
  def createFlowPanel(component: Component*): FlowPanel =
    val panel = SFlowPanel()
    panel.contents ++= component
    panel

  /** Creates a flow panel with shape effect. */
  case class SFlowPanel() extends FlowPanel with ShapeEffect with FontEffect:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)
    export layout._

  /** Creates a box panel with shape effect. */
  case class SBoxPanel(orientation: Orientation.Value) extends BoxPanel(orientation) with ShapeEffect

  /** Creates a panel with shape effect. */
  case class SPanel() extends Panel with ShapeEffect

  /** Creates a panel with picture effect and the given path. */
  def createPicturePanel(path: String): PicturePanel =
    val panel = PicturePanel()
    panel.picture = path
    panel

  /** Creates a panel with SVG effect and the given path. */
  def createSVGPanel(path: String): SVGPanel =
    val panel = SVGPanel()
    panel.svgIcon = path
    panel

  /** Creates a panel with picture effect. */
  case class PicturePanel() extends Panel with PictureEffect

  /** Creates a panel with SVG effect. */
  case class SVGPanel() extends Panel with SVGEffect

  /** Creates a button for the form, with the given text and rect. */
  def createFormButtonWith(text: String, rect: Styles.Rect): SButton =
    val button = SButton(text)
    button.rect = rect
    button.fontEffect = Styles.whiteFont
    button

  /** Creates a button for the form, with the given text. */
  case class SButton(label: String) extends Button(label) with ShapeEffect with FontEffect:
    focusPainted = false
    contentAreaFilled = false

  /** Creates a label for the form, with the given text. */
  case class SLabel(label: String) extends Label(label) with ShapeEffect with FontEffect

  /** Creates a text area for the form, with the given text. */
  case class STextField(colum: Int) extends TextField(colum) with ShapeEffect with FontEffect

  extension (panes: Map[ComposedImageLabel, Component])
    /** Returns [[STabbedPane]] given `panes`, a mapping of tab item [[ComposedImageLabel]] to theirs views. */
    def toTabbedPane: STabbedPane =
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

  /** A themed field label for given `fieldComponent` */
  class SFieldLabel[A <: Component](text: String)(fieldComponent: A) extends ComposedSwing:
    private val fieldBackground = Theme.light.background.withAlpha(50)
    private val mainPanel       = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val labelPanel      = ExtendedSwing.SFlowPanel()
    private val label           = ExtendedSwing.SLabel(text)

    fieldComponent.background = fieldBackground
    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += fieldComponent

    override def component[T >: Component]: T = mainPanel

  /** Creates a text area for the form, with the given text. */
  final case class STextArea() extends JTextArea:
    setFont(Styles.defaultFont.swingFont)
    setEditable(false)
    setOpaque(false)
