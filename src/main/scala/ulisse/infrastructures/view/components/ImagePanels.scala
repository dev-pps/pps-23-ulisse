package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter

import java.awt.Color
import javax.imageio.ImageIO
import scala.swing.{Graphics2D, Panel, UIElement}

object ImagePanels:
  trait ImagePanel extends Panel

  object ImagePanel:
    def createImagePanel(imagePath: String): ImagePanel                           = ImagePanelImpl(imagePath)
    def createSVGPanel(svgPath: String, color: Color): ImagePanel                 = SVGPanel(svgPath, color)
    def createDrawnPanel(iconDrawer: (UIElement, Graphics2D) => Unit): ImagePanel = DrawnPanel(iconDrawer)

  private final case class ImagePanelImpl(imagePath: String)
      extends ImagePanel:
    private val image = ImageIO.read(ClassLoader.getSystemResource(imagePath))
    override def paintComponent(g: Graphics2D): Unit =
      val size = math.min(peer.getWidth, peer.getHeight)
      g.drawImage(image, (peer.getWidth - size) / 2, (peer.getHeight - size) / 2, size, size, peer)

  private final case class SVGPanel(svgPath: String, color: Color)
      extends ImagePanel:
    private val rawIcon = new FlatSVGIcon(svgPath)
    rawIcon.setColorFilter(ColorFilter(_ => color))
    override def paintComponent(g: Graphics2D): Unit =
      val size = math.min(peer.getWidth, peer.getHeight)
      val icon = rawIcon.derive(size, size)
      icon.paintIcon(peer, g, (peer.getWidth - icon.getWidth) / 2, (peer.getHeight - icon.getHeight) / 2)

  private final case class DrawnPanel(iconDrawer: (UIElement, Graphics2D) => Unit)
      extends ImagePanel:
    override def paintComponent(g: Graphics2D): Unit = iconDrawer(this, g)

  object Example:
    def drawCross(preferredLength: Int, preferredThickness: Int, color: Color)(
        element: UIElement,
        g: Graphics2D
    ): Unit =
      val oldColor = g.getColor
      g.setColor(color)
      val center    = (element.size.width / 2, element.size.height / 2)
      val length    = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredLength)
      val thickness = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredThickness)
      g.fillRect(center._1 - thickness / 2, center._2 - length / 2, thickness, length)
      g.fillRect(center._1 - length / 2, center._2 - thickness / 2, length, thickness)
      g.setColor(oldColor)

    val imagePanelExample: ImagePanel = ImagePanel.createImagePanel("icon/logo.jpg")
    val svgPanelExample: ImagePanel   = ImagePanel.createSVGPanel("icon/map.svg", Color.RED)
    val drawnPanelExample: ImagePanel = ImagePanel.createDrawnPanel(drawCross(20, 2, Color.BLUE))
