package ulisse.infrastructures.view.components.ui.composed

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.ExtendedSwing.SVGPanel
import ulisse.infrastructures.view.components.ui.composed.ComposedImageLabel.SVGIconLabel
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt
import java.awt.Dimension
import scala.swing.*

/** Represents a composed swing component. */
trait ComposedSwing:
  /** Returns the main component. */
  def component[T >: Component]: T

object ComposedSwing:
  /** Creates a [[JInfoTextField]] from a [[title]]. */
  def createInfoTextField(text: String): JInfoTextField = JInfoTextField(text)

  /** Creates a [[JNavBar]] from a list of [[SVGIconLabel]]. */
  def createNavbar(JIconLabel: ComposedImageLabel*): JNavBar = JNavBar(JIconLabel: _*)

  /** Creates a [[JTabbedPane]] from a list of [[SVGIconLabel]]. */
  def createTabbedPane(JIconLabel: ComposedImageLabel*): JTabbedPane = JTabbedPane(JIconLabel: _*)

  /** Creates a [[JInsertForm]] from a [[title]] and a list of [[JInfoTextField]]. */
  def createInsertForm(title: String, JInfoTextField: JInfoTextField*): JInsertForm =
    JInsertForm(title, JInfoTextField: _*)

  /** Creates a [[JToggleIconButton]] from an on and off icon path. */
  def createToggleIconButton(onIconPath: String, offIconPath: String): JToggleIconButton =
    JToggleIconButton(onIconPath, offIconPath)

  /** Represents a text field with a [[title]] */
  case class JInfoTextField(title: String) extends ComposedSwing:
    private val colum               = 15
    private val textFieldPadding    = Styles.createPadding(10, 5)
    private val textFieldBackground = Theme.light.background.withAlpha(50)

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val label      = ExtendedSwing.JLabelItem(title)
    private val textField  = ExtendedSwing.JTextFieldItem(colum)

    textField.rect = textField.rect.withPadding(textFieldPadding)
    textField.rectPalette = textField.rectPalette.withBackground(textFieldBackground)

    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += textField

    export textField.text_=
    override def component[T >: Component]: T = mainPanel

  case class JNavBar(iconLabels: ComposedImageLabel*) extends ComposedSwing:
    private val padding = Styles.createPadding(40, 20)

    private val mainPanel = ExtendedSwing.JFlowPanelItem()
    mainPanel.rect = mainPanel.rect.withPadding(padding)
    mainPanel.hGap = 5

    mainPanel.contents ++= iconLabels.map(_.component)
    closeAll()

    iconLabels.foreach(icon =>
      icon.component.reactions += {
        case _: event.MousePressed =>
          closeAll()
          icon.showIconAndText()
      }
    )

    private def closeAll(): Unit              = iconLabels.foreach(_.showIcon())
    override def component[T >: Component]: T = mainPanel

  case class JTabbedPane(iconLabels: ComposedImageLabel*) extends ComposedSwing:
    private val mainPanel  = ExtendedSwing.JBorderPanelItem()
    private val pagesPanel = ExtendedSwing.JFlowPanelItem()

    private val navBar = createNavbar(iconLabels: _*)
    private val pages  = iconLabels.map(iconLabel => (iconLabel, ExtendedSwing.JFlowPanelItem())).toMap

    pages.values.foreach(_.visible = false)
    pagesPanel.contents ++= pages.values

    mainPanel.layout(navBar.component) = BorderPanel.Position.North
    mainPanel.layout(pagesPanel) = BorderPanel.Position.Center

    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case _: event.MousePressed =>
          pages.values.foreach(_.visible = false)
          paneOf(iconLabel).visible = true
      }
    )

    iconLabels.headOption.foreach(iconLabel => paneOf(iconLabel).visible = true)

    def paneOf(label: ComposedImageLabel): ExtendedSwing.JFlowPanelItem = pages(label)
    override def component[T >: Component]: T                           = mainPanel

  case class JInsertForm(title: String, infoTextField: JInfoTextField*) extends ComposedSwing:
    private val mainPanel = ExtendedSwing.JBorderPanelItem()
    private val formPanel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    val titleLabel        = ExtendedSwing.JLabelItem(title)

    private val titleSpace  = 5
    private val fieldsSpace = 15
    private val buttonSpace = 5

    private val fields = infoTextField.flatMap(field => List(field.component, Swing.VStrut(fieldsSpace)))

    formPanel.contents += Swing.VStrut(titleSpace)
    formPanel.contents ++= fields
    formPanel.contents += Swing.VStrut(buttonSpace)

    mainPanel.layout(titleLabel) = BorderPanel.Position.North
    mainPanel.layout(formPanel) = BorderPanel.Position.Center

    override def component[T >: Component]: T = mainPanel

  case class JToggleIconButton(onIconPath: String, offIconPath: String) extends ComposedSwing:
    private val mainPanel = ExtendedSwing.JFlowPanelItem()
    private val onIcon    = SVGPanel()
    private val offIcon   = SVGPanel()
    private val size      = 40

    offIcon.visible = false
    onIcon.svgIcon = onIconPath
    offIcon.svgIcon = offIconPath
    mainPanel.rect = Styles.iconButtonRect
    onIcon.svgIconPalette = Styles.iconOpenPalette
    offIcon.svgIconPalette = Styles.iconOpenPalette

    mainPanel.contents += onIcon
    mainPanel.contents += offIcon

    withDimension(size, size)

    mainPanel.reactions += { case _: event.MousePressed => toggle() }

    def toggle(): Unit =
      onIcon.visible = !onIcon.visible
      offIcon.visible = !offIcon.visible

    def withDimension(width: Int, height: Int): Unit =
      onIcon.preferredSize = Dimension(width, height)
      offIcon.preferredSize = Dimension(width, height)

    def withPadding(padding: Styles.Padding): Unit = mainPanel.rect = mainPanel.rect.withPadding(padding)

    override def component[T >: Component]: T = mainPanel
