package ulisse.infrastructures.view.components.ui.composed

import ulisse.infrastructures.view.components.ui.ExtendedSwing
import ulisse.infrastructures.view.components.ui.decorators.ImageEffects.{ImageEffect, PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.components.ui.decorators.Styles.Palette

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
  /** Represents a palette for SVG images. */
  trait SVGPalette(val palette: Palette, val iconPalette: Palette)

  /** Default palette when the label is open. */
  private val defaultSVGOpenPalette: SVGPalette = new SVGPalette(Styles.openLabelPalette, Styles.iconOpenPalette) {}

  /** Default palette when the label is closed. */
  private val defaultSVGClosePalette: SVGPalette = new SVGPalette(Styles.closeLabelPalette, Styles.iconClosePalette) {}

  /** Creates a [[PictureLabel]] from a [[path]], [[text]] and [[orientation]]. */
  def createPicture(path: String, text: String)(using orientation: Orientation.Value): ComposedImageLabel =
    new PictureLabel(path, text, orientation)(Styles.openLabelPalette, Styles.closeLabelPalette)

  /** Creates a [[PictureLabel]] from a [[path]] and [[text]] with a transparent background. */
  def createTransparentPicture(path: String, text: String)(using orientation: Orientation.Value): ComposedImageLabel =
    new PictureLabel(path, text, orientation)(Styles.transparentPalette, Styles.transparentPalette)

  /** Creates a [[SVGIconLabel]] from a [[path]], [[text]] and [[orientation]]. */
  def createIcon(iconPath: String, text: String)(using orientation: Orientation.Value): ComposedImageLabel =
    new SVGIconLabel(iconPath, text, orientation)(defaultSVGOpenPalette, defaultSVGClosePalette)

  /** Creates a [[SVGIconLabel]] from a [[path]] and [[text]] to be displayed in the dashboard. */
  def createToDashboard(iconPath: String, text: String)(using orientation: Orientation.Value): ComposedImageLabel =
    new SVGIconLabel(iconPath, text, orientation)(defaultSVGClosePalette, defaultSVGOpenPalette)

  /** Represents a label with an image and text. */
  private case class ImageLabel[I <: ImageEffect](image: I, text: String)(
      orientation: Orientation.Value,
      openPalette: Palette,
      closePalette: Palette
  ) extends ComposedImageLabel:
    private val defaultWidth  = 100
    private val defaultHeight = 40

    private val mainPanel  = ExtendedSwing.JBoxPanelItem(orientation)
    private val labelPanel = ExtendedSwing.JFlowPanelItem()
    private val label      = ExtendedSwing.JLabelItem(text)

    withDimension(defaultWidth, defaultHeight)

    mainPanel.rectPalette = openPalette
    label.rectPalette = Styles.transparentPalette
    labelPanel.rectPalette = Styles.transparentPalette

    labelPanel.contents += label
    mainPanel.contents += image
    mainPanel.contents += labelPanel

    image.listenTo(labelPanel.mouseEvents ++ mainPanel.mouseEvents ++ label.mouseEvents: _*)
    mainPanel.listenTo(labelPanel.mouseEvents ++ label.mouseEvents ++ image.mouseEvents: _*)

    override def showIconAndText(): Unit =
      label.visible = true
      mainPanel.rectPalette = openPalette
      withDimension(mainPanel.preferredSize.width, mainPanel.preferredSize.height)

    override def showIcon(): Unit =
      label.visible = false
      mainPanel.rectPalette = closePalette
      withDimension(mainPanel.preferredSize.height, mainPanel.preferredSize.height)

    override def withDimension(width: Int, height: Int): Unit =
      mainPanel.preferredSize = Dimension(width, height)
      image.preferredSize = Dimension(height, height)
      if orientation == Orientation.Horizontal then label.preferredSize = Dimension(width - height, height)

    override def component[T >: Component]: T = mainPanel

  /** Represents a label with a picture and text */
  private case class PictureLabel(pictureLabel: ImageLabel[PictureEffect]) extends ComposedImageLabel:
    def this(path: String, text: String, orientation: Orientation.Value)(openPalette: Palette, closePalette: Palette) =
      this(ImageLabel(ExtendedSwing.createPicturePanel(path), text)(orientation, openPalette, closePalette))

    export pictureLabel._

  /** Represents a label with an icon and text */
  private case class SVGIconLabel(svgIconLabel: ImageLabel[SVGEffect])(
      openIconPalette: Palette,
      closeIconPalette: Palette
  ) extends ComposedImageLabel:
    def this(path: String, text: String, orientation: Orientation.Value)(
        openPalette: SVGPalette,
        closePalette: SVGPalette
    ) = this(ImageLabel(ExtendedSwing.createSVGPanel(path), text)(
      orientation,
      openPalette.palette,
      closePalette.palette
    ))(openPalette.iconPalette, closePalette.iconPalette)

    svgIconLabel.image.svgIconPalette = openIconPalette

    export svgIconLabel.{showIcon => _, showIconAndText => _, _}

    override def showIconAndText(): Unit =
      svgIconLabel.image.svgIconPalette = openIconPalette
      svgIconLabel.showIconAndText()

    override def showIcon(): Unit =
      svgIconLabel.image.svgIconPalette = closeIconPalette
      svgIconLabel.showIcon()
