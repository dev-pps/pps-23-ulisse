package ulisse.infrastructures.view.components.ui.composed

import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.decorators.ImageEffects.{ImageEffect, PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt.Dimension
import scala.swing.{Component, Orientation}

/** Represents a label composed by an image and text. */
trait ComposedImageLabel extends ComposedSwing:
  /** Shows the icon and text. */
  def showIconAndText(): Unit

  /** Shows only the icon. */
  def showIcon(): Unit

  /** Sets the dimension of the label. */
  def withDimension(width: Int, height: Int): Unit

object ComposedImageLabel:
  /** Creates a [[PictureLabel]] from a [[path]], [[text]] and [[orientation]]. */
  def createPictureLabel(path: String, text: String)(using orientation: Orientation.Value): PictureLabel =
    new PictureLabel(path, text, orientation)

  /** Creates a [[SVGIconLabel]] from a [[path]], [[text]] and [[orientation]]. */
  def createIconLabel(iconPath: String, text: String)(using orientation: Orientation.Value): SVGIconLabel =
    new SVGIconLabel(iconPath, text, orientation)

  /** Represents a label with an image and text. */
  private case class ImageLabel[I <: ImageEffect](image: I, text: String, orientation: Orientation.Value)
      extends ComposedImageLabel:
    private val defaultWidth     = 100
    private val defaultHeight    = 40
    private val rectClosePalette = Styles.createPalette(Theme.light.overlay, Theme.light.click, Theme.light.click)
    private val rectOpenPalette  = rectClosePalette.withBackground(Theme.light.background.withAlpha(50))

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(orientation)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val label      = ExtendedSwing.JLabelItem(text)

    withDimension(defaultWidth, defaultHeight)

    mainPanel.rectPalette = rectOpenPalette
    label.rectPalette = Styles.transparentPalette
    labelPanel.rectPalette = Styles.transparentPalette

    labelPanel.contents += label
    mainPanel.contents += image
    mainPanel.contents += labelPanel

    image.listenTo(labelPanel.mouseEvents ++ mainPanel.mouseEvents ++ label.mouseEvents: _*)
    mainPanel.listenTo(labelPanel.mouseEvents ++ label.mouseEvents ++ image.mouseEvents: _*)

    override def showIconAndText(): Unit =
      label.visible = true
      mainPanel.rectPalette = rectOpenPalette
      withDimension(mainPanel.preferredSize.width, mainPanel.preferredSize.height)

    override def showIcon(): Unit =
      label.visible = false
      mainPanel.rectPalette = rectClosePalette
      withDimension(mainPanel.preferredSize.height, mainPanel.preferredSize.height)

    override def withDimension(width: Int, height: Int): Unit =
      mainPanel.preferredSize = Dimension(width, height)
      image.preferredSize = Dimension(height, height)
      if orientation == Orientation.Horizontal then label.preferredSize = Dimension(width - height, height)

    override def component[T >: Component]: T = mainPanel

  /** Represents a label with a picture and text */
  case class PictureLabel private (private val pictureLabel: ImageLabel[PictureEffect]) extends ComposedImageLabel:
    def this(path: String, text: String, orientation: Orientation.Value) =
      this(ImageLabel(ExtendedSwing.createPicturePanel(path), text, orientation))

    export pictureLabel._

  /** Represents a label with an icon and text */
  case class SVGIconLabel private (private val svgIconLabel: ImageLabel[SVGEffect]) extends ComposedImageLabel:
    def this(path: String, text: String, orientation: Orientation.Value) =
      this(ImageLabel(ExtendedSwing.createSVGPanel(path), text, orientation))

    private val iconClosePalette = Styles.defaultPalette.withBackground(Theme.light.background)
    private val iconOpenPalette  = Styles.createEqualPalette(Theme.light.background).withBackground(Theme.light.overlay)

    svgIconLabel.image.svgIconPalette = iconOpenPalette

    export svgIconLabel.component, svgIconLabel.withDimension

    override def showIconAndText(): Unit =
      svgIconLabel.image.svgIconPalette = iconOpenPalette
      svgIconLabel.showIconAndText()

    override def showIcon(): Unit =
      svgIconLabel.image.svgIconPalette = iconClosePalette
      svgIconLabel.showIcon()
