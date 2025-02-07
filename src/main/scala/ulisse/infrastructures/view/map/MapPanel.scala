package ulisse.infrastructures.view.map

import ulisse.entities.Coordinates
import ulisse.entities.Coordinates.UIPoint
import ulisse.infrastructures.view.map.ViewObservers.ViewObserver

import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import scala.math.BigDecimal.double2bigDecimal
import scala.math.{abs, sqrt}
import scala.swing.*

object MapPanel:
  def empty(): MapPanel = MapPanel()

  case class MapPanel() extends Panel:
    opaque = false

    private val items         = MapItemsCollection()
    private val mapObservable = ViewObservers.createObservable[Point]

    export mapObservable._

    listenTo(mouse.clicks, mouse.moves)
    reactions += {
      case event.MouseMoved(_, point, _) =>
        mapObservable.notifyOnHover(point)
        items.onHover(point)
        repaint()
      case event.MousePressed(_, point, _, _, _) =>
        mapObservable.notifyOnClick(point)
        items.onClick(point)
        repaint()
      case event.MouseReleased(_, point, _, _, _) =>
        mapObservable.notifyOnRelease(point)
        items.onRelease(point);
        repaint()
    }

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      items.draw(g, peer)

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
