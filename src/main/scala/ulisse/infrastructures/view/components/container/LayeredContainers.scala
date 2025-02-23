package ulisse.infrastructures.view.components.container

import ulisse.infrastructures.view.components.ui.ExtendedSwing.JBorderPanelItem

import javax.swing.JLayeredPane
import scala.swing.{BorderPanel, Component}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object LayeredContainers:
  trait JLayeredPane extends BorderPanel:
    private val layeredPane = JLayeredPane()

    private var _mainPane: Component  = JBorderPanelItem()
    private var _glassPane: Component = JBorderPanelItem()

    def mainPane: Component  = _mainPane
    def glassPane: Component = _glassPane

    def mainPane_=(component: Component): Unit  = _mainPane = component
    def glassPane_=(component: Component): Unit = _glassPane = component
//
//    layeredPane.add(mainPane.peer)
//    layeredPane.add(glassPane.peer, JLayeredPane.PALETTE_LAYER)
    layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center

    def add(component: Component): Unit =
      layeredPane.add(component.peer)
      revalidate()
      repaint()

//    override def revalidate(): Unit =
//      super.revalidate()
//      componentContents.revalidate()
//      mainPane.revalidate()
//      glassPane.revalidate()

//      val bounds = componentContents.getBounds()
//      val width  = bounds.getWidth.toInt
//      val height = bounds.getHeight.toInt
//      mainPane.peer.setBounds(0, 0, width, height)
//      glassPane.peer.setBounds(0, 0, width, height)

//    peer.addComponentListener(new java.awt.event.ComponentAdapter() {
//      override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
//        revalidate()
//        repaint()
//      }
//    })
