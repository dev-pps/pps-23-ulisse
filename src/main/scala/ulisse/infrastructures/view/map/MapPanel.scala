package ulisse.infrastructures.view.map

import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook

import java.awt.geom.AffineTransform
import scala.math.{abs, sqrt}
import scala.swing.*

/** Represent the map panel. */
trait MapPanel extends Panel with EnhancedLook:
  /** Draw the station on the screen. */
  def drawStation(newStations: StationPorts.Input#SM): Unit

/** Companion object for [[MapPanel]]. */
object MapPanel:
  /** Create a new [[MapPanel]]. */
  def apply(): MapPanel = MapPanelImpl()

  private case class MapPanelImpl() extends MapPanel:
    private val stations = MapElements[Station](observable)

    override def drawStation(newStations: StationPorts.Input#SM): Unit =
      stations.update(newStations.map(MapElement.createStation(_, ImagePath.station)))
      println(s"count = ${observable.clicks}")
      updateGraphics()

    override protected def paintLook(g: Graphics2D): Unit =
      stations.draw(g, peer)
      super.paintLook(g)

//    override protected def paintLook(g: Graphics2D): Unit =
//      super.paintLook(g)
//      itemCollection.draw(g, peer)

//      points.foreach((p1, p2) =>
//        g.setColor(java.awt.Color.BLACK)
//        val stationSize = 30
//        val half        = stationSize / 2
//        val scale       = 0.05
//
//        val ss: UIPoint =
//          Coordinate.uiPoint(p1._1.toDouble + (stationSize * scale), p1._2.toDouble + (stationSize * scale))
//        val es: UIPoint = Coordinate.uiPoint(p2._1.toDouble + (half * scale), p2._2.toDouble + (half * scale))
//        val start: (Double, Double) = (p1._1 - half, p1._2 - half)
//        val end: (Double, Double)   = (p2._1 - half, p2._2 - half)
//
//        drawTiledImage(g, routeImage, scale, ss, es)
//        g.drawImage(stationImage, start._1.toInt, start._2.toInt, 30, 30, peer)
//        g.drawImage(stationImage, end._1.toInt, end._2.toInt, 30, 30, peer)
//      )

    private def drawTiledImage(g: Graphics2D, img: Image, scale: Double, start: Coordinate, end: Coordinate): Unit =
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

        val x = start.x until end.x by stepX.toInt
        val y = start.y until end.y by stepY.toInt
        (x zip y).map((x, y) => (x.toDouble, y.toDouble))

      positions.foreach((x, y) =>
        val transform = new AffineTransform()
        transform.translate(x, y)
        transform.scale(scale, scale)
        transform.rotate(rotate)
        transform.translate(-scaleDim._1 / 2, -scaleDim._2 / 2)
        g.drawImage(img, transform, peer)
      )
