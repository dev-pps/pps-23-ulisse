package ulisse.infrastructures.view.components

import scala.swing.Color

object JColorPalette:

  def apply(background: Color, click: Color, hover: Color): JPalette = JPalette(background, click, hover)

  case class JPalette(background: Color, click: Color, hover: Color)
