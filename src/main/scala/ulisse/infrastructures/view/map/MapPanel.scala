package ulisse.infrastructures.view.map

import ulisse.entities.Coordinates
import ulisse.entities.Coordinates.{Coordinate, UIPoint}
import ulisse.entities.Route.Station

import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import javax.imageio.ImageIO
import scala.math.BigDecimal.double2bigDecimal
import scala.math.{abs, sqrt}
import scala.swing.{Graphics2D, Image, Panel}

trait MapPanel extends Panel:
  def setPoints(points: List[(Station, Station)]): Unit

object MapPanel:
  def apply(points: List[((Int, Int), (Int, Int))]): MapPanel = MapPanelImpl(points)
  def empty(): MapPanel                                       = MapPanelImpl(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapPanelImpl(var points: List[((Int, Int), (Int, Int))]) extends Panel with MapPanel:
    private val stationUrl   = ClassLoader.getSystemResource("station.png")
    private val stationImage = ImageIO.read(stationUrl)

    private val routeUrl   = getClass.getResource("/route.png")
    private val routeImage = ImageIO.read(routeUrl)

    opaque = false

    override def setPoints(points: List[((String, Coordinates.Geo), (String, Coordinates.Geo))]): Unit =
      this.points = points.map((p1, p2) =>
        ((p1._2.latitude.toInt, p1._2.longitude.toInt), (p2._2.latitude.toInt, p2._2.longitude.toInt))
      )

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      points.foreach((p1, p2) =>
        g.setColor(java.awt.Color.BLACK)
        val stationSize = 30
        val half        = stationSize / 2
        val scale       = 0.05

        val ss: UIPoint =
          Coordinate.uiPoint(p1._1.toDouble + (stationSize * scale), p1._2.toDouble + (stationSize * scale))
        val es: UIPoint = Coordinate.uiPoint(p2._1.toDouble + (half * scale), p2._2.toDouble + (half * scale))
        val start: (Double, Double) = (p1._1 - half, p1._2 - half)
        val end: (Double, Double)   = (p2._1 - half, p2._2 - half)

        drawTiledImage(g, routeImage, scale, ss, es)
        g.drawImage(stationImage, start._1.toInt, start._2.toInt, 30, 30, peer)
        g.drawImage(stationImage, end._1.toInt, end._2.toInt, 30, 30, peer)
      )

    private def drawTiledImage(g: Graphics2D, img: Image, scale: Double, start: UIPoint, end: UIPoint): Unit =
      val scaleDim = (img.getWidth(peer) * scale, img.getHeight(peer) * scale)
      val rotate   = start.angle(end)
      val diagonal = sqrt(scaleDim._1 * scaleDim._1 + scaleDim._2 * scaleDim._2)

      val positions: Seq[(Double, Double)] =
        val dx       = end.x - start.x
        val dy       = end.y - start.y
        val distance = start.distance(end)

        val correctedStep = diagonal - abs(diagonal - scaleDim._1)
        val stepX         = (dx / distance) * correctedStep
        val stepY         = (dy / distance) * correctedStep

        val x = start.x until end.x by stepX
        val y = start.y until end.y by stepY
        (x zip y).map((x, y) => (x.toDouble, y.toDouble))

      positions.foreach((x, y) =>
        val transform = new AffineTransform()
        transform.translate(x, y)
        transform.scale(scale, scale)
        transform.rotate(rotate)
        transform.translate(-scaleDim._1 / 2, -scaleDim._2 / 2)
        g.drawImage(img, transform, peer)
      )
