package ulisse.infrastructures.view.components

import scala.swing.Color

object JColorPalette:

  def apply(background: Color, click: Color, hover: Color): JPalette = JPalette(background, click, hover)

  case class JPalette(background: Color, click: Color, hover: Color):
    def setAll(newBackground: Color, newClick: Color, newHover: Color): JPalette =
      copy(background = newBackground, click = newClick, hover = newHover)

    def setBackground(newBackground: Color): JPalette = copy(background = newBackground)

    def setClick(newClick: Color): JPalette = copy(click = newClick)

    def setHover(newHover: Color): JPalette = copy(hover = newHover)
