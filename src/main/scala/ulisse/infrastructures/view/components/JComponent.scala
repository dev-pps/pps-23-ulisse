package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel.createSVGPanel

import java.awt
import java.awt.Dimension
import scala.swing.{event, Component, Orientation, Swing}

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

  private val labelStyler = JStyler.paletteStyler(JStyler.transparentPalette)
  private val elementStyler =
    JStyler.rectPaletteStyler(JStyler.defaultRect.copy(arc = 10), JStyler.backgroundPalette(Theme.light.overlayElement))

  case class JInfoTextField(text: String) extends JComponent:
    private val colum = 15

    private val styler =
      JStyler.rectPaletteStyler(
        JStyler.defaultRect.copy(padding = JStyler.createPadding(10, 5), arc = 10),
        JStyler.backgroundPalette(Theme.light.background.withAlpha(50))
      )

    private val mainPanel = JItem.createBoxPanel(Orientation.Vertical, JStyler.transparent)
    private val label     = JItem.label(text, labelStyler)
    private val textField = JItem.textField(colum, styler)

    private val northPanel = JItem.createFlowPanel(JStyler.transparent)
    northPanel.contents += label

    mainPanel.contents += northPanel
    mainPanel.contents += textField

    override def component[T >: Component]: T = mainPanel

  case class JIconLabel(iconPath: String, text: String) extends JComponent:
    private val width  = 100
    private val height = 40

    private val mainPanel = JItem.createBoxPanel(Orientation.Horizontal, elementStyler)
    private val icon      = createSVGPanel(iconPath, Theme.light.background)
    private val label     = JItem.label(text, labelStyler)

    icon.preferredSize = Dimension(height, height)
    label.preferredSize = Dimension(width, height)

    mainPanel.contents += icon
    mainPanel.contents += label

    def showIconAndText(): Unit = label.visible = true
    def showIcon(): Unit        = label.visible = false

    override def component[T >: Component]: T = mainPanel

  case class JNavBar(iconLabels: JIconLabel*) extends JComponent:
    private val mainPanel = JItem.createFlowPanel(JStyler.transparent)
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

    private val mainPanel = JItem.createBoxPanel(Orientation.Vertical, styler)
    private val navBar    = createNavbar(iconLabels: _*)
    private val panels: Map[JIconLabel, JItem.JFlowPanelItem] =
      iconLabels.map(iconLabel => (iconLabel, JItem.createFlowPanel(JStyler.transparent))).toMap

    panels.values.foreach(_.visible = false)

    mainPanel.contents += Swing.VGlue
    mainPanel.contents += navBar.component
    mainPanel.contents ++= panels.values
    mainPanel.contents += Swing.VGlue

    mainPanel.listenTo(navBar.component.mouse.clicks)
    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case event.MousePressed(_, _, _, _, _) =>
          panels.values.foreach(_.visible = false)
          paneOf(iconLabel).visible = true
      }
    )

    def paneOf(label: JIconLabel): JItem.JFlowPanelItem = panels(label)
    override def component[T >: Component]: T           = mainPanel

  case class JInsertForm(title: String, infoTextField: JInfoTextField*) extends JComponent:
    private val mainPanel  = JItem.createBoxPanel(Orientation.Vertical, JStyler.transparent)
    private val titlePanel = JItem.createFlowPanel(JStyler.transparent)
    private val formPanel  = JItem.createBoxPanel(Orientation.Vertical, JStyler.transparent)

    private val titleLabel = JItem.label(title, labelStyler)
    private val space      = 10

    titlePanel.contents += titleLabel
    formPanel.contents += titlePanel

    private val fields = infoTextField.flatMap(field => List(Swing.VStrut(space), field.component))
    formPanel.contents ++= fields

    mainPanel.contents += Swing.VStrut(space)
    mainPanel.contents += formPanel

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
