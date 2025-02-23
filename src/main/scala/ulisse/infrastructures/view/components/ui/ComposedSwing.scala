package ulisse.infrastructures.view.components.ui

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ui.ExtendedSwing.SVGPanel
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt
import java.awt.Dimension
import scala.swing.*
import scala.swing.MenuBar.NoMenuBar.mouse

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

  case class JIconLabel(iconPath: String, text: String) extends ComposedSwing:
    private val width  = 100
    private val height = 40

    private val rectClosePalette = Styles.createPalette(Theme.light.overlay, Theme.light.click, Theme.light.click)
    private val rectOpenPalette  = rectClosePalette.withBackground(Theme.light.background.withAlpha(50))

    private val iconClosePalette =
      Styles.createPalette(Theme.light.background, Theme.light.background, Theme.light.background)
    private val iconOpenPalette = iconClosePalette.withBackground(Theme.light.overlay)

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val icon       = ExtendedSwing.createSVGPanel(iconPath)
    private val label      = ExtendedSwing.JLabelItem(text)

    icon.preferredSize = Dimension(height, height)
    label.preferredSize = Dimension(width, height)

    icon.svgIconPalette = iconOpenPalette
    mainPanel.rectPalette = rectOpenPalette
    label.rectPalette = Styles.transparentPalette
    labelPanel.rectPalette = Styles.transparentPalette

    labelPanel.contents += label
    mainPanel.contents += icon
    mainPanel.contents += labelPanel

    icon.listenTo(mainPanel.mouseEvents ++ label.mouseEvents: _*)
    mainPanel.listenTo(label.mouseEvents ++ icon.mouseEvents: _*)
    mainPanel.reactions += {
      case _: event.MousePressed => if (label.visible) showIcon() else showIconAndText()
    }

    def showIconAndText(): Unit =
      label.visible = true
      icon.svgIconPalette = iconOpenPalette
      mainPanel.rectPalette = rectOpenPalette

    def showIcon(): Unit =
      label.visible = false
      icon.svgIconPalette = iconClosePalette
      mainPanel.rectPalette = rectClosePalette

    override def component[T >: Component]: T = mainPanel

  case class JNavBar(iconLabels: JIconLabel*) extends ComposedSwing:
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

    iconLabels.foreach(iconLabel =>
      iconLabel.component.reactions += {
        case event.MousePressed(_, _, _, _, _) =>
          pages.values.foreach(_.visible = false)
          paneOf(iconLabel).visible = true
      }
    )

    iconLabels.headOption.foreach(iconLabel => paneOf(iconLabel).visible = true)

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
    private val onIcon    = SVGPanel()
    onIcon.svgIcon = onIconPath
    onIcon.svgIconPalette = onIcon.svgIconPalette.withBackground(Theme.light.background)
    private val offIcon = SVGPanel()
    offIcon.svgIcon = offIconPath
    offIcon.svgIconPalette = offIcon.svgIconPalette.withBackground(Theme.light.background)
    private val size = 40

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
