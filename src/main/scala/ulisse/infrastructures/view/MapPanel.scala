package ulisse.infrastructures.view

import ulisse.entities.Coordinates
import ulisse.entities.Route.Station

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import scala.swing.{Graphics2D, Image, Panel}

trait MapPanel extends Panel:
  def setPoints(points: List[(Station, Station)]): Unit

object MapPanel:
  def apply(points: List[((Int, Int), (Int, Int))]): MapPanel = MapPanelImpl(points)
  def empty(): MapPanel                                       = MapPanelImpl(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class MapPanelImpl(var points: List[((Int, Int), (Int, Int))]) extends Panel with MapPanel:
    val inputStream = getClass.getResourceAsStream("/station.png")
    val image       = ImageIO.read(inputStream)

    override def setPoints(points: List[((String, Coordinates.Geo), (String, Coordinates.Geo))]): Unit =
      this.points = points.map((p1, p2) =>
        ((p1._2.latitude.toInt, p1._2.longitude.toInt), (p2._2.latitude.toInt, p2._2.longitude.toInt))
      )

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
//      final URL imgURL = ClassLoader.getSystemResource(path);
//      return new ImageIcon(imgURL).getImage();

//      inputStream.close()

//      val icon = new ImageIcon(image)
//      val imgURL = ClassLoader.getSystemResource("station.png")
//      println(imgURL)
//      val image = new ImageIcon(imgURL).getImage
//      println(image)

//      image.foreach(img => g.drawImage(img, 0, 0, size.width, size.height, _))

      points.foreach((p1, p2) =>
        g.setColor(java.awt.Color.GREEN)
        @SuppressWarnings(Array("org.wartremover.warts.Null"))
        val d = g.drawImage(image, p1._1 - 15, p1._2 - 15, 30, 30, null)
//        g.fillOval(p1._1 - 5, p1._2 - 5, 10, 10)
        g.setColor(java.awt.Color.BLACK)
        g.drawLine(p1._1, p1._2, p2._1, p2._2)
        g.setColor(java.awt.Color.GREEN)
        @SuppressWarnings(Array("org.wartremover.warts.Null"))
        val d1 = g.drawImage(image, p2._1 - 15, p2._2 - 15, 30, 30, null)
//        g.fillOval(p2._1 - 5, p2._2 - 5, 10, 10)
      )
    }
