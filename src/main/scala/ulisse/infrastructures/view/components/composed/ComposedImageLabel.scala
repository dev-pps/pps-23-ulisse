package ulisse.infrastructures.view.components.composed

import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.decorators.ImageEffects.{ImageEffect, PictureEffect, SVGEffect}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.components.styles.Styles.Palette
import ulisse.infrastructures.view.utils.ComponentUtils.*

import java.awt.Dimension
import scala.swing.event.MouseEvent
import scala.swing.{Alignment, Component, Orientation}

/** Represents a label composed by an image and text. */
trait ComposedImageLabel extends ComposedSwing with Observable[MouseEvent]:
  /** Returns true if the label is expanded. */
  def isExpanded: Boolean

  /** Shows the icon and text. */
  def showIconAndText(): Unit

  /** Shows only the icon. */
  def showIcon(): Unit

  /** Sets the font of the label. */
  def withFont(font: Styles.Font): Unit

  /** Sets the dimension of the label. */
  def withDimension(width: Int, height: Int): Unit

  /** Sets the padding of the label. */
  def withPadding(padding: Styles.Padding): Unit

  /** Sets the alignment of the label text. */
  def horizontalAlignment(alignment: Alignment.Value): Unit

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

  /** Creates a [[SVGIconLabel]] from a [[path]] and [[text]] to be displayed in the menu. */
  def createToMenu(iconPath: String, text: String)(using orientation: Orientation.Value): ComposedImageLabel =
    new SVGIconLabel(iconPath, text, orientation)(defaultSVGClosePalette, defaultSVGOpenPalette)

  /** Represents a label with an image and text. */
  private case class ImageLabel[I <: ImageEffect](image: I, text: String)(
      orientation: Orientation.Value,
      openPalette: Palette,
      closePalette: Palette
  ) extends ComposedImageLabel:
    private val defaultWidth  = 100
    private val defaultHeight = 40
    private val gap           = 5
    private val zeroGap       = 0

    private val mainPanel  = ExtendedSwing.SBoxPanel(orientation)
    private val labelPanel = ExtendedSwing.SFlowPanel()
    private val label      = ExtendedSwing.SLabel(text)

    withDimension(defaultWidth, defaultHeight)

    mainPanel.rectPalette = openPalette
    label.rectPalette = Styles.transparentPalette
    labelPanel.rectPalette = Styles.transparentPalette

    labelPanel.contents += label
    mainPanel.contents += image
    mainPanel.contents += labelPanel

    image.listenTo(labelPanel.mouseEvents ++ mainPanel.mouseEvents ++ label.mouseEvents: _*)
    mainPanel.listenTo(labelPanel.mouseEvents ++ label.mouseEvents ++ image.mouseEvents: _*)

    export mainPanel.observable._

    private def refreshGap(): Unit = orientation match
      case Orientation.Horizontal => labelPanel.vGap = zeroGap; labelPanel.hGap = gap
      case Orientation.Vertical   => labelPanel.hGap = zeroGap; labelPanel.vGap = gap
      case _                      => ()

    export mainPanel.rectPadding_= as withPadding, label.fontEffect_= as withFont,
      label.horizontalAlignment_= as horizontalAlignment, labelPanel.visible as isExpanded

    override def showIconAndText(): Unit =
      labelPanel.visible = true
      mainPanel.rectPalette = openPalette
      withDimension(mainPanel.preferredSize.width, mainPanel.preferredSize.height)

    override def showIcon(): Unit =
      labelPanel.visible = false
      mainPanel.rectPalette = closePalette
      withDimension(mainPanel.preferredSize.height, mainPanel.preferredSize.height)

    override def withDimension(width: Int, height: Int): Unit =
      mainPanel.preferredSize = Dimension(width, height)
      image.preferredSize = Dimension(height, height)
      if orientation == Orientation.Horizontal then
        label.preferredSize = Dimension(width - height, height)
        labelPanel.preferredSize = Dimension(width - height, height)
      refreshGap()

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
