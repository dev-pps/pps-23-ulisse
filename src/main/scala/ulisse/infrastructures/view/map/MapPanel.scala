package ulisse.infrastructures.view.map

import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.infrastructures.view.common.{ImagePath, Observers}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.components.draw.DrawImages

import java.awt.geom.AffineTransform
import scala.math.{abs, sqrt}
import scala.swing.*
import scala.swing.event.MouseEvent

trait MapPanel extends Panel with EnhancedLook:
  def drawStation(stations: StationPorts.Input#SM): Unit

object MapPanel:
  def empty(): MapPanel = MapPanelImpl()

  private case class MapPanelImpl() extends MapPanel:
    opaque = false

    private val itemCollection = MapItemsCollection()
    private val mapObservable  = Observers.createObservable[Point]

    private val image = DrawImages.createAt(ImagePath.station, new Point(500, 500))

    attach(image.toObserver(data => data))

    export itemCollection.{attach as attachItem, detach as detachItem}

    reactions += {
      case event: event.MouseMoved =>
        image.notifyHover(event)
        updateGraphics()
//      case event.MousePressed(_, point, _, _, _) =>
//        itemCollection.onClick(point)
//        updateGraphics()
//      case event.MouseReleased(_, point, _, _, _) =>
//        itemCollection.onRelease(point)
//        updateGraphics()
    }

    override def drawStation(stations: StationPorts.Input#SM): Unit = ()

    override protected def paintLook(g: Graphics2D): Unit =
      image.draw(g, peer)
      itemCollection.draw(g, peer)
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
