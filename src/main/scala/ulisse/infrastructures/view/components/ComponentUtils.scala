package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.JStyler

import java.awt
import java.awt.{BorderLayout, Color, Graphics}
import javax.swing.{DefaultButtonModel, Icon, JToggleButton}
import javax.swing.JToggleButton.ToggleButtonModel
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicRadioButtonUI
import scala.swing.event.MouseClicked
import scala.swing.{
  AbstractButton,
  BoxPanel,
  ButtonGroup,
  Component,
  Container,
  Dimension,
  Graphics2D,
  Orientation,
  Panel,
  Publisher,
  RadioButton,
  SequentialContainer,
  Swing,
  ToggleButton,
  UIElement
}

object ComponentUtils:
  private val emptyIcon = new Icon:
    override def getIconWidth: Int                                              = 0
    override def getIconHeight: Int                                             = 0
    override def paintIcon(c: awt.Component, g: Graphics, x: Int, y: Int): Unit = ()

  extension [E <: UIElement](element: E)
    def fixedSize(width: Int, height: Int): E =
      element.preferredSize = new Dimension(width, height)
      element.maximumSize = new Dimension(width, height)
      element.minimumSize = new Dimension(width, height)
      element

  extension [C <: Component](component: C)
    def alignLeft(): Component =
      val wrapper = JPanel.createBox(Orientation.Horizontal)
      wrapper.contents += component
      wrapper.contents += Swing.HGlue
      wrapper

    def alignRight(): Component =
      val wrapper = JPanel.createBox(Orientation.Horizontal)
      wrapper.contents += Swing.HGlue
      wrapper.contents += component
      wrapper

    def alignTop(): Component =
      val wrapper = JPanel.createBox(Orientation.Vertical)
      wrapper.contents += component
      wrapper.contents += Swing.VGlue
      wrapper

    def alignBottom(): Component =
      val wrapper = JPanel.createBox(Orientation.Vertical)
      wrapper.contents += Swing.VGlue
      wrapper.contents += component
      wrapper

    def centerHorizontally(): Component =
      alignLeft().alignRight()

    def centerVertically(): Component =
      alignTop().alignBottom()

    def center(): Component =
      centerHorizontally().centerVertically()

    def genericClickReaction(action: () => Unit): C =
      component.listenTo(component.mouse.clicks)
      component.reactions += {
        case _: MouseClicked => action()
      }
      component

    def visible(value: Boolean): C =
      component.visible = value
      component

    def opaque(value: Boolean): C =
      component.opaque = value
      component

    @Deprecated("Use JStyler Border")
    def defaultBorder(): C =
      component.border = new LineBorder(Color.BLACK, 2)
      component

    def fListenTo(ps: Publisher*): C =
      component.listenTo(ps*)
      component

    def makeSelectable(): RadioButton =
      new RadioButton():
        peer.setUI(new BasicRadioButtonUI { override def getDefaultIcon: Icon = emptyIcon })
        peer.add(component.peer, BorderLayout.CENTER)
        listenTo(component.mouse.clicks)
        reactions += {
          case _: MouseClicked =>
            peer.doClick()
        }
        peer.getModel.addChangeListener(_ => {
          println(s"changed: ${peer.getModel.isSelected}")
          component.background = if peer.getModel.isSelected then Color.LIGHT_GRAY else Color.WHITE
        })

  extension [B <: AbstractButton](button: B)
    def addToGroup(buttonGroup: ButtonGroup): B =
      buttonGroup.peer.add(button.peer)
      button

  extension [JC <: JComponent](jc: JC)
    def styler(styler: JStyler): JC =
      jc.setStyler(styler)
      jc
