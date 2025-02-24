package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.components.ComponentConfigurations.Alignment
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.components.ui.decorators.SwingEnhancements.EnhancedLook

import java.awt
import java.awt.{BorderLayout, Color, Graphics}
import javax.swing.JToggleButton.ToggleButtonModel
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicRadioButtonUI
import javax.swing.{DefaultButtonModel, Icon, JToggleButton}
import scala.swing.event.MouseClicked
import scala.swing.*

object ComponentUtils:
  extension [E <: UIElement](element: E)
    def fixedSize(width: Int, height: Int): E =
      element.preferredSize = new Dimension(width, height)
      element.maximumSize = new Dimension(width, height)
      element.minimumSize = new Dimension(width, height)
      element

  extension [C <: Component](component: C)
    def align(alignment: Option[Alignment]): Component = alignment match
      case Some(value) => value match
          case Alignment.Left   => component.alignLeft()
          case Alignment.Right  => component.alignRight()
          case Alignment.Top    => component.alignTop()
          case Alignment.Bottom => component.alignBottom()
          case Alignment.Center => component.center()
      case None => component

    def alignLeft(): Component =
      val wrapper = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
      wrapper.contents += component
      wrapper.contents += Swing.HGlue
      wrapper

    def alignRight(): Component =
      val wrapper = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
      wrapper.contents += Swing.HGlue
      wrapper.contents += component
      wrapper

    def alignTop(): Component =
      val wrapper = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
      wrapper.contents += component
      wrapper.contents += Swing.VGlue
      wrapper

    def alignBottom(): Component =
      val wrapper = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
      wrapper.contents += Swing.VGlue
      wrapper.contents += component
      wrapper

    def centerHorizontally(): Component =
      alignLeft().alignRight()

    def centerVertically(): Component =
      alignTop().alignBottom()

    def center(): Component =
      centerHorizontally().centerVertically()

    // tenere d'occhio
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

//  extension [J <: EnhancedLook](j: J)
//    def styler(styler: JStyle.JStyler): J = j
