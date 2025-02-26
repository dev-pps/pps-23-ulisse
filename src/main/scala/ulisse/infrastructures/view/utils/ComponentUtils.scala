package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.ComposedSwing
import ulisse.infrastructures.view.components.ui.decorators.Styles

import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object ComponentUtils:

  /** Utils method to center a component */
  extension (component: Component)

    /** Center the component horizontally */
    def centerHorizontally(): ExtendedSwing.JFlowPanelItem =
      val panel = ExtendedSwing.JFlowPanelItem()
      panel.rectPalette = Styles.transparentPalette
      panel.contents += component
      panel

    /** Center the component vertically */
    def centerVertically(): ExtendedSwing.JBoxPanelItem =
      val panel = ExtendedSwing.JBoxPanelItem(Orientation.Vertical)
      panel.rectPalette = Styles.transparentPalette
      panel.contents += Swing.VGlue
      panel.contents += component
      panel

    /** Center the component */
    def center(): Panel = centerHorizontally().centerVertically()

    /** Create a component with a left and right component */
    def createLeftRight(right: Component): ExtendedSwing.JBoxPanelItem =
      val panel = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
      panel.rectPalette = Styles.transparentPalette
      panel.contents += component.centerVertically()
      panel.contents += Swing.HGlue
      panel.contents += right.centerVertically()
      panel

  /** Utils method to center a composed component */
  extension (composed: ComposedSwing)

    /** Center the component horizontally */
    def centerHorizontally(): ExtendedSwing.JFlowPanelItem = composed.component.centerHorizontally()

    /** Center the component vertically */
    def centerVertically(): ExtendedSwing.JBoxPanelItem = composed.component.centerVertically()

    /** Center the component */
    def center(): Panel = centerHorizontally().centerVertically()

    /** Create a component with a left and right component */
    def createLeftRight(right: ComposedSwing): ExtendedSwing.JBoxPanelItem =
      composed.component.createLeftRight(right.component)
