package ulisse.infrastructures.view.components

import javax.swing.{JLayeredPane, JRootPane}
import scala.swing.{BorderPanel, Component}
import ulisse.infrastructures.view.components.ComponentUtils.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object LayeredContainers:
  trait JLayeredPane extends BorderPanel:
    private val componentContents = JLayeredPane()

    private var _mainPane: Component  = BorderPanel()
    private var _glassPane: Component = BorderPanel().opaque(false)

    def mainPane: Component  = _mainPane
    def glassPane: Component = _glassPane

    def mainPane_=(component: Component): Unit  = _mainPane = component
    def glassPane_=(component: Component): Unit = _glassPane = component

    componentContents.add(mainPane.peer)
    componentContents.add(glassPane.peer, JLayeredPane.PALETTE_LAYER)
    layout(Component.wrap(componentContents)) = BorderPanel.Position.Center

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
