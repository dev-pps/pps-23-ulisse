package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel.createSVGPanel

import java.awt
import java.awt.Dimension
import scala.swing.*

trait JComponent:
  def component[T >: Component]: T

object JComponent:
  def createInfoTextField(text: String): JInfoTextField           = JInfoTextField(text)
  def createIconLabel(iconPath: String, text: String): JIconLabel = JIconLabel(iconPath, text)
  def createNavbar(JIconLabel: JIconLabel*): JNavBar              = JNavBar(JIconLabel: _*)
  def createTabbedPane(JIconLabel: JIconLabel*): JTabbedPane      = JTabbedPane(JIconLabel: _*)

  def createInsertForm(title: String, JInfoTextField: JInfoTextField*): JInsertForm =
    JInsertForm(title, JInfoTextField: _*)
  def createToggleIconButton(onIconPath: String, offIconPath: String): JToggleIconButton =
    JToggleIconButton(onIconPath, offIconPath)

  private val elementStyler =
    JStyler.rectPaletteStyler(JStyler.defaultRect.copy(arc = 10), JStyler.backgroundPalette(Theme.light.overlayElement))

  case class JInfoTextField(text: String) extends JComponent:
    private val colum = 15

    private val textFieldStyler =
      JStyler.rectPaletteStyler(
        JStyler.defaultRect.copy(padding = JStyler.createPadding(10, 5), arc = 10),
        JStyler.backgroundPalette(Theme.light.background.withAlpha(50))
      )

    private val mainPanel  = JItem.createBoxPanel(Orientation.Vertical, JStyler.transparent)
    private val labelPanel = JItem.createFlowPanel(JStyler.transparent)

    private val label     = JItem.label(text, JStyler.transparent)
    private val textField = JItem.textField(colum, textFieldStyler)

    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += textField

    override def component[T >: Component]: T = mainPanel

  case class JIconLabel(iconPath: String, text: String) extends JComponent:
    private val width  = 100
    private val height = 40

    private val openStyler = JStyler.rectPaletteStyler(
      JStyler.defaultRect.copy(arc = 10),
      JStyler.palette(Theme.light.background.withAlpha(50), Theme.light.forwardClick, Theme.light.forwardClick)
    )
    private val closeStyler = JStyler.rectPaletteStyler(
      JStyler.defaultRect.copy(arc = 10),
      JStyler.palette(Theme.light.overlayElement, Theme.light.forwardClick, Theme.light.forwardClick)
    )

    private val mainPanel = JItem.createBoxPanel(Orientation.Horizontal, openStyler)
    private val icon      = createSVGPanel(iconPath, Theme.light.background)
    private val label     = JItem.label(text, JStyler.transparent)

    icon.preferredSize = Dimension(height, height)
    label.preferredSize = Dimension(width, height)

    mainPanel.listenTo(label.mouse.clicks, label.mouse.moves, icon.mouse.clicks, icon.mouse.moves)

    mainPanel.contents += icon
    mainPanel.contents += label

    mainPanel.reactions += {
      case event.MouseEntered(_, _, _) => icon.color = Theme.light.background
    }

    def showIconAndText(): Unit =
      icon.color = Theme.light.overlayElement
      mainPanel.setStyler(openStyler)
      label.visible = true

    def showIcon(): Unit =
      icon.color = Theme.light.background
      mainPanel.setStyler(closeStyler)
      label.visible = false

    override def component[T >: Component]: T = mainPanel

  case class JNavBar(iconLabels: JIconLabel*) extends JComponent:
    private val styler =
      JStyler.transparent.copy(rect = JStyler.defaultRect.copy(padding = JStyler.createPadding(40, 20)))

    private val mainPanel = JItem.createFlowPanel(styler)
    mainPanel.hGap = 5

    mainPanel.contents ++= iconLabels.map(_.component)
    iconLabels.foreach(_.showIcon())

    mainPanel.listenTo(mainPanel.mouse.clicks)
    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case event.MousePressed(_, _, _, _, _) =>
          iconLabels.foreach(_.showIcon())
          iconLabel.showIconAndText()
      }
    )

    override def component[T >: Component]: T = mainPanel

  case class JTabbedPane(iconLabels: JIconLabel*) extends JComponent:
    private val styler =
      JStyler.rectPaletteStyler(JStyler.defaultRect.copy(arc = 10), JStyler.backgroundPalette(Theme.light.element))

    private val mainPanel  = JItem.createBorderPanel(styler)
    private val pagesPanel = JItem.createFlowPanel(JStyler.transparent)

    private val navBar = createNavbar(iconLabels: _*)
    private val pages  = iconLabels.map(iconLabel => (iconLabel, JItem.createFlowPanel(JStyler.transparent))).toMap

    pages.values.foreach(_.visible = false)
    pagesPanel.contents ++= pages.values

    mainPanel.layout(navBar.component) = BorderPanel.Position.North
    mainPanel.layout(pagesPanel) = BorderPanel.Position.Center

    mainPanel.listenTo(navBar.component.mouse.clicks)
    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case event.MousePressed(_, _, _, _, _) =>
          pages.values.foreach(_.visible = false)
          paneOf(iconLabel).visible = true
      }
    )

    def paneOf(label: JIconLabel): JItem.JFlowPanelItem = pages(label)
    override def component[T >: Component]: T           = mainPanel

  case class JInsertForm(title: String, infoTextField: JInfoTextField*) extends JComponent:
    private val mainPanel = JItem.createBorderPanel(JStyler.transparent)
    private val formPanel = JItem.createBoxPanel(Orientation.Vertical, JStyler.transparent)
    val titleLabel        = JItem.label(title, JStyler.transparent)

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

  case class JToggleIconButton(onIconPath: String, offIconPath: String) extends JComponent:
    private val mainPanel = JItem.createFlowPanel(JStyler.transparent)
    private val onIcon    = createSVGPanel(onIconPath, Theme.light.background)
    private val offIcon   = createSVGPanel(offIconPath, Theme.light.background)
    private val size      = 40

    onIcon.preferredSize = Dimension(size, size)
    offIcon.preferredSize = Dimension(size, size)
    offIcon.visible = false

    mainPanel.listenTo(mainPanel.mouse.clicks)
    mainPanel.reactions += {
      case event.MousePressed(_, _, _, _, _) => toggle()
    }

    mainPanel.contents += onIcon
    mainPanel.contents += offIcon

    private def toggle(): Unit =
      onIcon.visible = !onIcon.visible
      offIcon.visible = !offIcon.visible

    override def component[T >: Component]: T = mainPanel
