package ulisse.infrastructures.view.components.styles

import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.components.styles.Styles.Palette

import java.awt.Color
import scala.swing.{event, Reactions}

/** Represent the current color of the component. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class CurrentColor(private var _current: Color):
  /** Return the current color. */
  def current: Color = _current

  /** Set the current color. */
  def current_=(color: Color): Unit = _current = color

  /** Set the current color from the palette when the component is hovered. */
  def hoverColor(palette: Styles.Palette): Unit = palette.hoverColor.foreach(current = _)

  /** Set the current color from the palette when the component is clicked. */
  def clickColor(palette: Styles.Palette): Unit = palette.clickColor.foreach(current = _)

  /** Set the current color from the palette when the component is exited. */
  def exitColor(palette: Styles.Palette): Unit = palette.hoverColor.foreach(_ => current = palette.background)

  /** Set the current color from the palette when the component is released. */
  def releaseColor(palette: Styles.Palette): Unit = current = palette.hoverColor getOrElse palette.background

  /** Initialize the color reactions of the component. */
  def initColorReactions(component: EnhancedLook, palette: () => Palette): Reactions.Reaction =
    case _: event.MousePressed  => clickColor(palette()); component.updateGraphics()
    case _: event.MouseReleased => releaseColor(palette()); component.updateGraphics()
    case _: event.MouseEntered  => hoverColor(palette()); component.updateGraphics()
    case _: event.MouseExited   => exitColor(palette()); component.updateGraphics()
