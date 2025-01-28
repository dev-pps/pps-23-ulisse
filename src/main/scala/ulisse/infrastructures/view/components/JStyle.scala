package ulisse.infrastructures.view.components

import scala.swing.Color

object JStyle:

  trait ColorPalette:
    val background: Color
    val click: Color
    val hover: Color
    def setAll(newBackground: Color, newClick: Color, newHover: Color): ColorPalette
    def setBackground(newBackground: Color): ColorPalette
    def setClick(newClick: Color): ColorPalette
    def setHover(newHover: Color): ColorPalette

  object ColorPalette:

    def apply(background: Color, click: Color, hover: Color): ColorPalette =
      ColorPaletteImpl(background, click, hover)

    private case class ColorPaletteImpl(background: Color, click: Color, hover: Color) extends ColorPalette:
      def setAll(newBackground: Color, newClick: Color, newHover: Color): ColorPaletteImpl =
        copy(background = newBackground, click = newClick, hover = newHover)

      def setBackground(newBackground: Color): ColorPaletteImpl = copy(background = newBackground)
      def setClick(newClick: Color): ColorPaletteImpl           = copy(click = newClick)
      def setHover(newHover: Color): ColorPaletteImpl           = copy(hover = newHover)

  trait Border:
    val color: Color
    val width: Int
    val arc: Int
    def setAll(newColor: Color, newWidth: Int, newArc: Int): Border
    def setColor(newColor: Color): Border
    def setWidth(newWidth: Int): Border
    def setArc(newArc: Int): Border

  object Border:
    def apply(color: Color, width: Int, arc: Int): Border = BorderImpl(color, width, arc)

    private case class BorderImpl(color: Color, width: Int, arc: Int) extends Border:
      def setAll(newColor: Color, newWidth: Int, newArc: Int): BorderImpl =
        copy(color = newColor, width = newWidth, arc = newArc)

      def setColor(newColor: Color): BorderImpl = copy(color = newColor)
      def setWidth(newWidth: Int): BorderImpl   = copy(width = newWidth)
      def setArc(newArc: Int): BorderImpl       = copy(arc = newArc)
