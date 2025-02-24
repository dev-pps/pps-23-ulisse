package ulisse.infrastructures.view.components.ui.composed

import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction.{Horizontal, Vertical}
import ulisse.infrastructures.view.components.ui.decorators.ImageEffects.{ImageEffect, PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt.Dimension
import scala.swing.{Component, Orientation}

object ComposedImage:
  enum Direction(val orientation: Orientation.Value, val width: Int, val height: Int):
    case Horizontal extends Direction(Orientation.Horizontal, 100, 40)
    case Vertical   extends Direction(Orientation.Vertical, 100, 100)

  def createPictureLabel(path: String, text: String)(using direction: Direction): PictureLabel =
    new PictureLabel(path, text, direction)
  def createIconLabel(iconPath: String, text: String)(using direction: Direction): SVGIconLabel =
    new SVGIconLabel(iconPath, text, direction)

  /** Represents a label with an image and text */
  private case class ImageLabel[I <: ImageEffect](image: I, text: String, direction: Direction) extends ComposedSwing:
    private val rectClosePalette = Styles.createPalette(Theme.light.overlay, Theme.light.click, Theme.light.click)
    private val rectOpenPalette  = rectClosePalette.withBackground(Theme.light.background.withAlpha(50))

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(direction.orientation)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val label      = ExtendedSwing.JLabelItem(text)

    mainPanel.preferredSize = Dimension(direction.width, direction.height)
    image.preferredSize = Dimension(direction.height, direction.height)
    if direction == Horizontal then label.preferredSize = Dimension(direction.height, direction.height)

    mainPanel.rectPalette = rectOpenPalette
    label.rectPalette = Styles.transparentPalette
    labelPanel.rectPalette = Styles.transparentPalette

    labelPanel.contents += label
    mainPanel.contents += image
    mainPanel.contents += labelPanel

    image.listenTo(labelPanel.mouseEvents ++ mainPanel.mouseEvents ++ label.mouseEvents: _*)
    mainPanel.listenTo(labelPanel.mouseEvents ++ label.mouseEvents ++ image.mouseEvents: _*)

    def showIconAndText(): Unit =
      label.visible = true
      mainPanel.rectPalette = rectOpenPalette

    def showIcon(): Unit =
      label.visible = false
      mainPanel.rectPalette = rectClosePalette

    override def component[T >: Component]: T = mainPanel

  /** Represents a label with a picture and text */
  case class PictureLabel private (private val pictureLabel: ImageLabel[PictureEffect]) extends ComposedSwing:
    def this(path: String, text: String, direction: Direction) =
      this(ImageLabel(ExtendedSwing.createPicturePanel(path), text, direction))

    export pictureLabel.component

  /** Represents a label with an icon and text */
  case class SVGIconLabel private (private val svgIconLabel: ImageLabel[SVGEffect]) extends ComposedSwing:
    def this(path: String, text: String, direction: Direction) =
      this(ImageLabel(ExtendedSwing.createSVGPanel(path), text, direction))

    private val iconClosePalette =
      Styles.createPalette(Theme.light.background, Theme.light.background, Theme.light.background)
    private val iconOpenPalette = iconClosePalette.withBackground(Theme.light.overlay)
    export svgIconLabel.component

    svgIconLabel.image.svgIconPalette = iconOpenPalette

    def showIconAndText(): Unit =
      svgIconLabel.image.svgIconPalette = iconOpenPalette
      svgIconLabel.showIconAndText()

    def showIcon(): Unit =
      svgIconLabel.image.svgIconPalette = iconClosePalette
      svgIconLabel.showIconAndText()
