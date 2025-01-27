package ulisse.infrastructures.view

import ulisse.entities.Coordinates
import ulisse.entities.Route.Station

import java.awt.RenderingHints
import javax.imageio.ImageIO
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

    override def setPoints(points: List[((String, Coordinates.Geo), (String, Coordinates.Geo))]): Unit =
      this.points = points.map((p1, p2) =>
        ((p1._2.latitude.toInt, p1._2.longitude.toInt), (p2._2.latitude.toInt, p2._2.longitude.toInt))
      )

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      drawTiledImage(g, routeImage, (30, 30), (400, 400), (500, 500))

      points.foreach((p1, p2) =>
        g.setColor(java.awt.Color.BLACK)
        g.drawLine(p1._1, p1._2, p2._1, p2._2)

        val d  = g.drawImage(stationImage, p1._1 - 15, p1._2 - 15, 30, 30, peer)
        val d1 = g.drawImage(stationImage, p2._1 - 15, p2._2 - 15, 30, 30, peer)
      )
    }

    private def drawTiledImage(g: Graphics2D, img: Image, dimension: (Int, Int), start: (Int, Int), end: (Int, Int)) =
      val x   = start._1 until end._1 by dimension._1
      val y   = start._2 until end._2 by dimension._2
      val pos = x zip y

      pos.foreach((x, y) => g.drawImage(img, x, y, dimension._1, dimension._2, peer))
