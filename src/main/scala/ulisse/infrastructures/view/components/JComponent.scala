package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel.createSVGPanel

import java.awt.{Dimension, FlowLayout}
import scala.swing.{event, Component, Orientation, Panel}

trait JComponent:
  def component[T >: Component]: T

object JComponent:
  def createInfoTextField(text: String): JInfoTextField           = JInfoTextField(text)
  def createIconLabel(iconPath: String, text: String): JIconLabel = JIconLabel(iconPath, text)
  def createNavBar(JIconLabel: JIconLabel*): JNavBar              = JNavBar(JIconLabel: _*)
  def createTabbedPane(JIconLabel: JIconLabel*): JTabbedPane      = JTabbedPane(JIconLabel: _*)

  private val transparentStyler = JStyler.paletteStyler(JStyler.transparentPalette)
  private val labelStyler       = JStyler.paletteStyler(JStyler.transparentPalette)
  private val elementStyler =
    JStyler.rectPaletteStyler(JStyler.roundRect(10), JStyler.backgroundPalette(Theme.light.element))

  case class JInfoTextField(text: String) extends JComponent:
    private val mainPanel = JItem.createBoxPanel(Orientation.Vertical, transparentStyler)
    private val label     = JItem.label(text, labelStyler)
    private val textField = JItem.textField(10, elementStyler)

    private val northPanel = JItem.createFlowPanel(transparentStyler)
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
    private val mainPanel = JItem.createFlowPanel(transparentStyler)
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
    private val mainPanel = JItem.createBoxPanel(Orientation.Vertical, transparentStyler)
    private val navBar    = createNavBar(iconLabels: _*)
    private val panels: Map[JIconLabel, JItem.JFlowPanelItem] =
      iconLabels.map(iconLabel => (iconLabel, JItem.createFlowPanel(transparentStyler))).toMap

    panels.values.foreach(_.visible = false)

    mainPanel.contents += navBar.component
    mainPanel.contents ++= panels.values

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
