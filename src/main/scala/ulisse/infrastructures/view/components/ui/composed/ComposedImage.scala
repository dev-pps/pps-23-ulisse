package ulisse.infrastructures.view.components.ui.composed

import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.decorators.ImageEffects.{ImageEffect, PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt.Dimension
import scala.swing.{Component, Orientation}

object ComposedImage:
  def createPictureLabel(path: String, text: String): PictureLabel  = new PictureLabel(path, text)
  def createIconLabel(iconPath: String, text: String): SVGIconLabel = new SVGIconLabel(iconPath, text)

  /** Represents a label with an image and text */
  private case class ImageLabel[I <: ImageEffect](image: I, text: String) extends ComposedSwing:
    private val width  = 100
    private val height = 40

    private val rectClosePalette = Styles.createPalette(Theme.light.overlay, Theme.light.click, Theme.light.click)
    private val rectOpenPalette  = rectClosePalette.withBackground(Theme.light.background.withAlpha(50))

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(Orientation.Horizontal)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val label      = ExtendedSwing.JLabelItem(text)

    image.preferredSize = Dimension(height, height)
    label.preferredSize = Dimension(width, height)

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
    def this(path: String, text: String) = this(ImageLabel(ExtendedSwing.createPicturePanel(path), text))
    export pictureLabel.component

  /** Represents a label with an icon and text */
  case class SVGIconLabel private (private val svgIconLabel: ImageLabel[SVGEffect]) extends ComposedSwing:
    def this(path: String, text: String) = this(ImageLabel(ExtendedSwing.createSVGPanel(path), text))
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
