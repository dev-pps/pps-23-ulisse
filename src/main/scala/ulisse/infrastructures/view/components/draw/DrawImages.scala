package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.{ClickObserver, MovedObserver, Observable, ReleaseObserver}
import ulisse.infrastructures.view.components.styles.Styles

import java.awt.image.ImageObserver
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images on the screen. */
object DrawImages:

  /** Default dimension for images. */
  val defaultDimension: Dimension = new Dimension(30, 30)

  /** Default value for silhouette. */
  val defaultScaleSilhouette: Float = 1.4f

  /** Represent a generic image. */
  trait DrawImage extends Observable[MouseEvent] with ClickObserver[MouseEvent] with ReleaseObserver[MouseEvent]
      with MovedObserver[MouseEvent]:
    /** Center of the image. */
    val center: Point

    /** Dimension of the image. */
    val dimension: Dimension

    /** Observe the mouse event. */
    val observable: Observable[MouseEvent]

    /** Scale of the image. */
    def scale: Float

    /** Set the value of the image. */
    def scale_=(value: Float): Unit

    /** Silhouette of the image. */
    def silhouettePalette: Styles.Palette

    /** Set the silhouette palette of the image. */
    def silhouettePalette_=(palette: Styles.Palette): Unit

    /** Draw the image on the screen. */
    def draw(g: Graphics2D, observer: ImageObserver): Unit

    /** Attach the image to the observable. */
    def attachOn(observable: Observable[MouseEvent]): Unit =
      observable.attachMove(this)
      observable.attachClick(this)
      observable.attachRelease(this)

    /** Detach the image from the observable. */
    def detachFrom(observable: Observable[MouseEvent]): Unit =
      observable.detachMove(this)
      observable.detachClick(this)
      observable.detachRelease(this)
