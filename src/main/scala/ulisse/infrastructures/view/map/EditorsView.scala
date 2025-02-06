package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.form.CentralController
import ulisse.infrastructures.view.menu.Menu

import javax.swing.JLayeredPane
import scala.swing.{BorderPanel, Component, Dimension}
import scala.swing.BorderPanel.Position.{Center, East}

final case class EditorsView() extends BorderPanel:
  preferredSize = new Dimension(800, 800)
  private val componentContents = JLayeredPane()
  private val mainPane          = BorderPanel()
  private val glassPane         = BorderPanel()

  val mapPark: MapPanel = MapPanel.empty()
  val mapController     = CentralController.createMap()

  mainPane.layout(mapPark) = Center
  glassPane.layout(mapController.component) = East

  componentContents.add(mainPane.peer)
  componentContents.add(glassPane.peer, JLayeredPane.PALETTE_LAYER)

  layout(Component.wrap(componentContents)) = Center

  override def revalidate(): Unit =
    super.revalidate()
    val bounds = componentContents.getBounds()
    val width  = bounds.getWidth.toInt
    val height = bounds.getHeight.toInt
    mainPane.peer.setBounds(0, 0, width, height)
    glassPane.peer.setBounds(0, 0, width, height)

  peer.addComponentListener(new java.awt.event.ComponentAdapter() {
    override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
      revalidate()
      repaint()
    }
  })
