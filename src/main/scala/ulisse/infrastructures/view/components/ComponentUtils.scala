package ulisse.infrastructures.view.components

import java.awt.{BorderLayout, Color}
import javax.swing.{DefaultButtonModel, JToggleButton}
import javax.swing.JToggleButton.ToggleButtonModel
import javax.swing.border.LineBorder
import scala.swing.event.MouseClicked
import scala.swing.{AbstractButton, ButtonGroup, Component, Dimension, Graphics2D, Publisher, UIElement}

object ComponentUtils:
  extension [E <: UIElement](element: E)
    def fixedSize(width: Int, height: Int): E =
      element.preferredSize = new Dimension(width, height)
      element.maximumSize = new Dimension(width, height)
      element.minimumSize = new Dimension(width, height)
      element

  extension [C <: Component](component: C)
    def genericClickReaction(action: () => Unit): C =
      component.listenTo(component.mouse.clicks)
      component.reactions += {
        case _: MouseClicked => action()
      }
      component

    def opaque(value: Boolean): C =
      component.opaque = value
      component

    def defaultBorder(): C =
      component.border = new LineBorder(Color.BLACK, 2)
      component

    def fListenTo(ps: Publisher*): C =
      component.listenTo(ps*)
      component

    def makeSelectable(): AbstractButton =
      new AbstractButton:
        peer.setModel(new ToggleButtonModel)
        peer.add(component.peer, BorderLayout.CENTER)
        this.opaque = true
        listenTo(mouse.clicks)
        reactions += {
          case m: MouseClicked =>
            peer.getModel.setSelected(true)
        }

        peer.getModel.addChangeListener(_ => {
          val isSelected = peer.getModel.isSelected
          println(if (isSelected) "Selected" else "Not Selected")
          peer.setBackground(if (isSelected) Color.LIGHT_GRAY else Color.WHITE)
          repaint()
        })

  extension [B <: AbstractButton](button: B)
    def addToGroup(buttonGroup: ButtonGroup): B =
      buttonGroup.peer.add(button.peer)
      button
