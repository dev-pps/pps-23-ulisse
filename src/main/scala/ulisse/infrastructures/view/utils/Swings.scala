package ulisse.infrastructures.view.utils

import ulisse.entities.Coordinate
import ulisse.infrastructures.view.components.draw.DrawImages.DrawImage

import java.awt.geom.Point2D
import java.awt.image.ImageObserver
import javax.swing.BorderFactory
import javax.swing.border.Border
import scala.concurrent.ExecutionContext
import scala.swing.*

/** Utility methods for Swing components */
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object Swings:

  /** The execution context for the Swing components. */
  given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
    Swing.onEDT(runnable.run())

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

    /** Multiply the point by the given [[value]]. */
    def times(value: Double): Point = new Point((point.x * value).toInt, (point.y * value).toInt)

    /** Divide the point by the given [[value]]. */
    def divide(value: Double): Point2D.Double = new Point2D.Double((point.x / value).toInt, (point.y / value).toInt)

    /** Calculate the distance between the point and the other. */
    def distance(other: Point): Double =
      math sqrt ((math pow (point.x - other.x, 2)) + (math pow (point.y - other.y, 2)))

    /** Calculate the angle between the point and the other. */
    def angle(other: Point): Double = math atan2 (other.y - point.y, other.x - point.x)

    /** Transform the point to a [[Dimension]] object. */
    def toDimension: Dimension = new Dimension(point.x, point.y)

    /** Transform the point to a [[Point2D]] object. */
    def toPointDouble: Point2D.Double = new Point2D.Double(point.x, point.y)

    /** Calculate the hypotenuse of the point. */
    def hypot: Double = math hypot (point.x, point.y)

    /** Check if the point is inside the rectangle. */
    def isPointInRotatedRectangle(a: Point, b: Point, width: Double): Boolean =
      val endMinusStart = b minus a
      val normal        = endMinusStart.toPointDouble divide endMinusStart.hypot
      val perp          = new Point2D.Double(-normal.y, normal.x)

      val halfWidth = width / 2
      val height    = endMinusStart.hypot

      val p1 = a.toPointDouble plus (perp times halfWidth)
      val p2 = a.toPointDouble minus (perp times halfWidth)
      val p3 = p2 plus (normal times height)
      val p4 = p1 plus (normal times height)

      point.toPointDouble isPointInPolygon Array(p1, p2, p3, p4)

    /** Check if the point has collided with the [[image]]. */
    def hasCollided(image: DrawImage): Boolean =
      val x          = point.x
      val y          = point.y
      val itemX      = image.center.x
      val itemY      = image.center.y
      val itemWidth  = image.dimension.width
      val itemHeight = image.dimension.height
      x >= itemX && x <= itemX + itemWidth && y >= itemY && y <= itemY + itemHeight

  /** Methods to perform arithmetic operations on [[Point2D.Double]] objects */
  extension (point: Point2D.Double)
    /** Plus the point to the other. */
    def plus(other: Point2D.Double): Point2D.Double = new Point2D.Double(point.x + other.x, point.y + other.y)

    /** Subtract the point from the other. */
    def minus(other: Point2D.Double): Point2D.Double = new Point2D.Double(point.x - other.x, point.y - other.y)

    /** Multiply the point by the other. */
    def times(other: Point2D.Double): Point2D.Double = new Point2D.Double(point.x * other.x, point.y * other.y)

    /** Multiply the point by the given [[value]]. */
    def times(values: Double) = new Point2D.Double(point.x * values, point.y * values)

    /** Multiply the point by the other. */
    def divide(value: Double): Point2D.Double = new Point2D.Double(point.x / value, point.y / value)

    /** Normalize the point. */
    def normalize: Point2D.Double = point divide math.hypot(point.x, point.y)

    /** Calculate the perpendicular of the point. */
    def perp: Point2D.Double = new Point2D.Double(-point.y, point.x)

    /** Transform the point to a [[Point]] object. */
    def toPoint: Point = new Point(point.x.toInt, point.y.toInt)

    /** Check if the point is inside the polygon. */
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    def isPointInPolygon(poly: Array[Point2D.Double]): Boolean =
      val (x, y) = (point.x, point.y)
      var inside = false
      var j      = poly.length - 1
      for (i <- poly.indices)
        val xi = poly(i).x
        val yi = poly(i).y
        val xj = poly(j).x
        val yj = poly(j).y
        if ((yi > y) != (yj > y) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) inside = !inside
        j = i
      inside

  /** Methods to perform arithmetic operations on [[Dimension]] objects */
  extension (dimension: Dimension)
    /** Subtract dimension from the other. */
    def minus(other: Dimension): Dimension =
      new Dimension(dimension.width - other.width, dimension.height - other.height)

    /** Add the dimension to the other. */
    def plus(other: Dimension): Dimension =
      new Dimension(dimension.width + other.width, dimension.height + other.height)

    /** Add the dimension to the given [[scale]]. */
    def plus(scale: Double): Dimension =
      new Dimension((dimension.width + scale).toInt, (dimension.height + scale).toInt)

    /** Multiply the dimension by the other. */
    def times(other: Dimension): Dimension =
      new Dimension(dimension.width * other.width, dimension.height * other.height)

    /** Multiply the dimension by the given [[value]]. */
    def times(value: Double): Dimension =
      new Dimension((dimension.width * value).toInt, (dimension.height * value).toInt)

    /** Divide the dimension by the given [[value]]. */
    def divide(value: Double): Dimension =
      new Dimension((dimension.width / value).toInt, (dimension.height / value).toInt)

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
