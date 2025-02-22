package ulisse.infrastructures.view.components.ui

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.Images.ImagePanel.createSVGPanel
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt
import java.awt.Dimension
import scala.swing.*

trait ComposedSwing:
  def component[T >: Component]: T

object ComposedSwing:
  def createInfoTextField(text: String): JInfoTextField           = JInfoTextField(text)
  def createIconLabel(iconPath: String, text: String): JIconLabel = JIconLabel(iconPath, text)
  def createNavbar(JIconLabel: JIconLabel*): JNavBar              = JNavBar(JIconLabel: _*)
  def createTabbedPane(JIconLabel: JIconLabel*): JTabbedPane      = JTabbedPane(JIconLabel: _*)

  def createInsertForm(title: String, JInfoTextField: JInfoTextField*): JInsertForm =
    JInsertForm(title, JInfoTextField: _*)
  def createToggleIconButton(onIconPath: String, offIconPath: String): JToggleIconButton =
    JToggleIconButton(onIconPath, offIconPath)

  private val elementPalette: Styles.Palette = Styles.defaultPalette.withClickColor(Theme.light.overlayElement)

  case class JInfoTextField(title: String) extends ComposedSwing:
    private val colum                      = 15
    private val textFieldRect: Styles.Rect = Styles.defaultRect.withPaddingWidthAndHeight(10, 5)
    private val paletteRect: Styles.Palette =
      Styles.defaultPalette.withBackground(Theme.light.background.withAlpha(50))

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()

    private val label     = ExtendedSwing.JLabelItem(title)
    private val textField = ExtendedSwing.JTextFieldItem(colum)
    textField.rect = textFieldRect
    textField.rectPalette = paletteRect

    labelPanel.contents += label
    mainPanel.contents += labelPanel
    mainPanel.contents += textField

    export textField.text_=
    override def component[T >: Component]: T = mainPanel

  case class JIconLabel(iconPath: String, text: String) extends ComposedSwing:
    private val width  = 100
    private val height = 40

    private val openPalette: Styles.Palette =
      Styles.createPalette(Theme.light.background.withAlpha(50), Theme.light.forwardClick, Theme.light.forwardClick)
    private val closePalette: Styles.Palette =
      Styles.createPalette(Theme.light.overlayElement, Theme.light.forwardClick, Theme.light.forwardClick)

    private val mainPanel = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
    mainPanel.rectPalette = openPalette

    private val icon  = createSVGPanel(iconPath, Theme.light.background)
    private val label = ExtendedSwing.JLabelItem(text)

    icon.preferredSize = Dimension(height, height)
    label.preferredSize = Dimension(width, height)

    mainPanel.listenTo(label.mouse.clicks, label.mouse.moves, icon.mouse.clicks, icon.mouse.moves)

    mainPanel.contents += icon
    mainPanel.contents += label

    mainPanel.reactions += {
      case event.MouseEntered(_, _, _) => icon.color = Theme.light.background
      case event.MouseExited(_, _, _) =>
        val color = if (label.visible) Theme.light.overlayElement else Theme.light.background
        icon.color = color
      case event.MousePressed(_, _, _, _, _) =>
        if (label.visible) showIcon() else showIconAndText()
      case event.MouseReleased(_, _, _, _, _) =>
        if (label.visible) showIconAndText() else showIcon()
    }

    def showIconAndText(): Unit =
      icon.color = Theme.light.overlayElement
      label.visible = true
      mainPanel.rectPalette = openPalette
      mainPanel.repaint()
      mainPanel.validate()

    def showIcon(): Unit =
      icon.color = Theme.light.background
      label.visible = false
      mainPanel.rectPalette = closePalette
      mainPanel.repaint()
      mainPanel.validate()

    override def component[T >: Component]: T = mainPanel

  case class JNavBar(iconLabels: JIconLabel*) extends ComposedSwing:
    private val panelRect: Styles.Rect = Styles.defaultRect.withPaddingWidthAndHeight(40, 20)

    private val mainPanel = ExtendedSwing.JFlowPanelItem()
    mainPanel.rect = panelRect
    mainPanel.hGap = 5

    mainPanel.contents ++= iconLabels.map(_.component)
    iconLabels.foreach(_.showIcon())

    mainPanel.listenTo(mainPanel.mouse.clicks)
    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case _: event.MousePressed =>
          iconLabels.foreach(_.showIcon())
          iconLabel.showIconAndText()
      }
    )

    override def component[T >: Component]: T = mainPanel

  case class JTabbedPane(iconLabels: JIconLabel*) extends ComposedSwing:
    private val panelPalette: Styles.Palette = Styles.defaultPalette.withBackground(Theme.light.element)

    private val mainPanel = ExtendedSwing.JBorderPanelItem()
    mainPanel.rectPalette = panelPalette
    private val pagesPanel = ExtendedSwing.JFlowPanelItem()

    private val navBar = createNavbar(iconLabels: _*)
    private val pages  = iconLabels.map(iconLabel => (iconLabel, ExtendedSwing.JFlowPanelItem())).toMap

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

    pages.headOption.foreach((key, _) => paneOf(key))
    def paneOf(label: JIconLabel): ExtendedSwing.JFlowPanelItem = pages(label)
    override def component[T >: Component]: T                   = mainPanel

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
