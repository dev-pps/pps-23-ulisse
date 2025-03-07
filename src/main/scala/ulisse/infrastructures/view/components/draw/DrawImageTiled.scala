package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages.{defaultDimension, DrawImage}
import ulisse.infrastructures.view.components.styles.Images
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.ImageObserver
import scala.math.{abs, sqrt}
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images tiled on the screen. */
trait DrawImageTiled extends DrawImage:

  /** Draw the tiled image on the screen. */
  def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit

/** Companion object for [[DrawImageTiled]]. */
object DrawImageTiled:

  /** Create a new [[DrawImageTiled]]. */
  def apply(path: String, start: Point, end: Point, dimension: Dimension): DrawImageTiled =
    new DrawImageTiledImpl(path, start, end, dimension)

  /** Create a new [[DrawImageTiled]] with the default dimension. */
  def createAt(path: String, start: Point, end: Point): DrawImageTiled =
    DrawImageTiled(path, start, end, defaultDimension)

  private case class DrawImageTiledImpl(source: SourceImage, start: Point, end: Point, dimension: Dimension)
      extends DrawImageTiled:
    def this(path: String, start: Point, end: Point, dimension: Dimension) =
      this(SourceImage(path), start, end, dimension)

    private val image: DrawImageSimple              = DrawImageSimple(source.path, start, dimension)
    override val center: Point                      = start plus end times 0.5
    override val observable: Observable[MouseEvent] = image.observable

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var width = 0

    scale = 0.05

    export image.{center => _, dimension => _, draw => _, observable => _, _}

    override def onMove(data: MouseEvent): Unit =
      if test(data.point.x, data.point.y, start.x, start.y, end.x, end.y, width) then
        println("CIAO")

    override def draw(g: Graphics2D, observer: ImageObserver): Unit =
      drawTiledImage(g, scale, observer)

    def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit =
      for img <- source.bufferImage
      yield
        val scaleDim = new Dimension((img.getWidth(observer) * scale).toInt, (img.getHeight(observer) * scale).toInt)
        val rotate   = start angle end
        val diagonal = sqrt(scaleDim.width * scaleDim.width + scaleDim.height * scaleDim.height)

        width = scaleDim.width

        g.setColor(Color.red)
        g.fillOval(center.x, center.y, 5, 5)

        g.setColor(Color.blue)
        g.fillOval(start.x, start.y, 5, 5)

        g.setColor(Color.green)
        g.fillOval(end.x, end.y, 5, 5)

        val positions: Seq[(Double, Double)] =
          val dx       = end.x - start.x
          val dy       = end.y - start.y
          val distance = start distance end

          val correctedStep = diagonal - abs(diagonal - scaleDim.width)
          val stepX         = (dx / distance) * correctedStep
          val stepY         = (dy / distance) * correctedStep

          val x = start.x until end.x by stepX.toInt
          val y = start.y until end.y by stepY.toInt
          (x zip y).map((x, y) => (x.toDouble, y.toDouble))

        positions.foreach((x, y) =>
          val transform = new AffineTransform()
          transform translate (x, y)
          transform scale (scale, scale)
          transform rotate rotate
          transform translate (-scaleDim.width, -scaleDim.height)
          g drawImage (img, transform, observer)
        )

    import java.awt.geom.{AffineTransform, Path2D, Point2D}

    def isPointInRotatedRectangle(
        mousePoint: Point,
        a: Point,
        b: Point,
        width: Double
    ): Boolean =
      // Calcola il centro del rettangolo
      val c = (a plus b) times 0.5

      // Calcola l'angolo di rotazione
      val angle = a angle b

      // Crea il rettangolo di base non ruotato
      val rect = new Path2D.Double()
      rect.moveTo(-width / 2, -Point2D.distance(a.x, a.y, b.x, b.y) / 2)
      rect.lineTo(width / 2, -Point2D.distance(a.x, a.y, b.x, b.y) / 2)
      rect.lineTo(width / 2, Point2D.distance(a.x, a.y, b.x, b.y) / 2)
      rect.lineTo(-width / 2, Point2D.distance(a.x, a.y, b.x, b.y) / 2)
      rect.closePath()

      // Applica la trasformazione di rotazione e traslazione
      val transform = new AffineTransform()
      transform.translate(c.x, c.y)
      transform.rotate(angle)

      val transformedRect = transform.createTransformedShape(rect)

      // Verifica se il punto è dentro il rettangolo ruotato
      transformedRect.contains(mousePoint)

    def test(px: Double, py: Double, ax: Double, ay: Double, bx: Double, by: Double, width: Double): Boolean = {

      // Calcola il vettore direzione da A a B
      val dx     = bx - ax
      val dy     = by - ay
      val length = Math.hypot(dx, dy)

      // Normalizza il vettore direzione
      val ux = dx / length
      val uy = dy / length

      // Calcola il vettore perpendicolare
      val perpX = -uy * (width / 2)
      val perpY = ux * (width / 2)

      // Calcola i quattro vertici del rettangolo
      val p1 = new Point2D.Double(ax + perpX, ay + perpY)
      val p2 = new Point2D.Double(ax - perpX, ay - perpY)
      val p3 = new Point2D.Double(bx - perpX, by - perpY)
      val p4 = new Point2D.Double(bx + perpX, by + perpY)

      // Funzione per testare se il punto è dentro il rettangolo usando il prodotto vettoriale
      def isPointInPolygon(point: Point2D.Double, poly: Array[Point2D.Double]): Boolean = {
        val (x, y) = (point.getX, point.getY)
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var inside = false
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var j = poly.length - 1
        for (i <- poly.indices) {
          val xi = poly(i).getX
          val yi = poly(i).getY
          val xj = poly(j).getX
          val yj = poly(j).getY
          if (
            (yi > y) != (yj > y) &&
            (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
          ) {
            inside = !inside
          }
          j = i
        }
        inside
      }

      // Controlla se il punto è dentro il rettangolo
      isPointInPolygon(new Point2D.Double(px, py), Array(p1, p2, p3, p4))
    }
