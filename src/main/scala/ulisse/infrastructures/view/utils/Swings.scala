package ulisse.infrastructures.view.utils

import ulisse.entities.Coordinate
import ulisse.infrastructures.view.components.draw.DrawImages.DrawImage

import java.awt.image.ImageObserver
import javax.swing.BorderFactory
import javax.swing.border.Border
import scala.swing.*

/** Utility methods for Swing components */
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object Swings:
  /** Create an empty border with the given [[width]] and [[height]]. */
  def createEmptyBorder(width: Int, height: Int): Border = BorderFactory.createEmptyBorder(height, width, height, width)

  /** Methods to adapt the [[Coordinate]] object in the [[Point]] object. */
  extension (coordinate: Coordinate)
    /** Transform the [[Coordinate]] to a [[Point]] object. */
    def toPoint: Point = new Point(coordinate.x, coordinate.y)

  /** Methods to improve the [[Container]] object. */
  extension (a: Container)
    /** Center the [[Container]] in the [[Container]] [[b]]. */
    def centerOf(b: Container): Unit =
      val dialogWidth: Int  = a.size.width
      val dialogHeight: Int = a.size.height
      val x: Int            = b.location.x + (b.size.width - dialogWidth) / 2
      val y: Int            = b.location.y + (b.size.height - dialogHeight) / 2
      a.peer.setLocation(x, y)

  /** Methods to perform arithmetic operations on [[Point]] objects */
  extension (point: Point)
    /** Subtract point from the other. */
    def minus(other: Point): Point = new Point(point.x - other.x, point.y - other.y)

    /** Add the point to the other. */
    def plus(other: Point): Point = new Point(point.x + other.x, point.y + other.y)

    /** Multiply the point by the other. */
    def times(other: Point): Point = new Point(point.x * other.x, point.y * other.y)

    /** Calculate the distance between the point and the other. */
    def distance(other: Point): Double = Math.sqrt(Math.pow(point.x - other.x, 2) + Math.pow(point.y - other.y, 2))

    /** Calculate the angle between the point and the other. */
    def angle(other: Point): Double = Math.atan2(other.y - point.y, other.x - point.x)

    /** Transform the point to a [[Dimension]] object. */
    def toDimension: Dimension = new Dimension(point.x, point.y)

    def hasCollided(image: DrawImage): Boolean =
      val x          = point.x
      val y          = point.y
      val itemX      = image.center.x
      val itemY      = image.center.y
      val itemWidth  = image.dimension.width
      val itemHeight = image.dimension.height
      x >= itemX && x <= itemX + itemWidth && y >= itemY && y <= itemY + itemHeight

  /** Methods to perform arithmetic operations on [[Dimension]] objects */
  extension (dimension: Dimension)
    /** Subtract dimension from the other. */
    def minus(other: Dimension): Dimension =
      new Dimension(dimension.width - other.width, dimension.height - other.height)

    /** Add the dimension to the other. */
    def plus(other: Dimension): Dimension =
      new Dimension(dimension.width + other.width, dimension.height + other.height)

    /** Multiply the dimension by the other. */
    def times(other: Dimension): Dimension =
      new Dimension(dimension.width * other.width, dimension.height * other.height)

    /** Transform the dimension to a [[Point]] object. */
    def toPoint: Point = new Point(dimension.width, dimension.height)

  /** Methods to perform operations on a [[Graphics2D]] object */
  extension (g: Graphics2D)
    /** Draw the image from the [[JImage]] object. */
    def drawImage(image: DrawImage, observer: ImageObserver): Unit = image.draw(g, observer)

  /** Methods to perform arithmetic operations on a [[(Point, Dimension)]] object */
  extension (shape: (Point, Dimension))
    /** Scale the shape by the given [[scale]]. */
    def scaleOf(scale: Float): (Point, Dimension) =
      val (center, dimension) = shape
      val scaleSize           = new Dimension((dimension.width * scale).toInt, (dimension.height * scale).toInt)
      val newDimension        = scaleSize.plus(new Dimension(scaleSize.width % 2, scaleSize.height % 2))
      val differentSize       = newDimension.minus(dimension)
      val newPosition         = center.minus(new Point(differentSize.width / 2, differentSize.height / 2))
      (newPosition, newDimension)
