package ulisse.infrastructures.view

import ulisse.entities.Coordinates
import ulisse.entities.Route.Station

import java.awt.{Image, RenderingHints}
import java.awt.image.ImageObserver
import javax.imageio.ImageIO
import scala.swing.{Graphics2D, Panel}

trait MapPanel extends Panel:
  def setPoints(points: List[(Station, Station)]): Unit

object MapPanel:
  def apply(points: List[((Int, Int), (Int, Int))]): MapPanel = MapPanelImpl(points)
  def empty(): MapPanel                                       = MapPanelImpl(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapPanelImpl(var points: List[((Int, Int), (Int, Int))]) extends Panel with MapPanel:
    val url = ClassLoader.getSystemResource("station.png")
//    val url = getClass.getResource("/station.png")
//    val inputStream = getClass.getResourceAsStream("/station.png")
    val image = ImageIO.read(url)
//    inputStream.close()

    override def setPoints(points: List[((String, Coordinates.Geo), (String, Coordinates.Geo))]): Unit =
      this.points = points.map((p1, p2) =>
        ((p1._2.latitude.toInt, p1._2.longitude.toInt), (p2._2.latitude.toInt, p2._2.longitude.toInt))
      )

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

//      final URL imgURL = ClassLoader.getSystemResource(path);
//      return new ImageIcon(imgURL).getImage();

//      inputStream.close()

//      val icon = new ImageIcon(image)
//      val imgURL = ClassLoader.getSystemResource("station.png")
//      println(imgURL)
//      val image = new ImageIcon(imgURL).getImage
//      println(image)

      points.foreach((p1, p2) =>
        g.setColor(java.awt.Color.BLACK)
        g.drawLine(p1._1, p1._2, p2._1, p2._2)

        val d  = g.drawImage(image, p1._1 - 15, p1._2 - 15, 30, 30, this.peer)
        val d1 = g.drawImage(image, p2._1 - 15, p2._2 - 15, 30, 30, this.peer)
      )
    }
