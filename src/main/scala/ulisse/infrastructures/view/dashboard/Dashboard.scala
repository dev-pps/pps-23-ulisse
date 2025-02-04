package ulisse.infrastructures.view.dashboard
import ulisse.infrastructures.view.{StationSettings, UpdatableContainer}
import ulisse.infrastructures.view.components.ComponentUtils.*

import java.awt.{Color, LayoutManager2}
import javax.swing.{JLayeredPane, OverlayLayout}
import scala.swing.event.MouseClicked
import scala.swing.{BorderPanel, BoxPanel, Component, Dimension, Label, Orientation, Panel, Rectangle, Swing}

//final case class CustomLayoutManager() extends LayoutManager2:
//  override def addLayoutComponent(name: String, comp: Component): Unit = ()
//  override def removeLayoutComponent(comp: Component): Unit = ()
//  override def preferredLayoutSize(parent: java.awt.Container): java.awt.Dimension = new java.awt.Dimension(600, 400)
//  override def minimumLayoutSize(parent: java.awt.Container): java.awt.Dimension = new java.awt.Dimension(600, 400)
//  override def maximumLayoutSize(parent: java.awt.Container): java.awt.Dimension = new java.awt.Dimension(600, 400)
//  override def addLayoutComponent(comp: Component, constraints: Any): Unit = ()
//  override def invalidateLayout(target: Container): Unit = ()
//  override def layoutContainer(parent: java.awt.Container): Unit = ()
//  override def getLayoutAlignmentX(parent: java.awt.Container): Float = 0.5f
//  override def getLayoutAlignmentY(parent: java.awt.Container): Float = 0.5f

final case class Dashboard(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val dashboardContent = new JLayeredPane()
  val centerComponent  = StationSettings().stationEditorView
  val sideMenu         = SideMenu()
  sideMenu.background = Color.GREEN
  dashboardContent.add(centerComponent.peer)
  dashboardContent.add(sideMenu.peer, JLayeredPane.PALETTE_LAYER)

  layout(Component.wrap(dashboardContent)) = BorderPanel.Position.Center

  override def revalidate(): Unit =
    super.revalidate()
    println("revalidateee")
    val bounds = dashboardContent.getBounds()
    val width  = bounds.getWidth.toInt
    val height = bounds.getHeight.toInt
    println(sideMenu.peer.getLayout.minimumLayoutSize(sideMenu.peer))
    println(sideMenu.peer.getLayout.preferredLayoutSize(sideMenu.peer))
    sideMenu.peer.setBounds(0, 0, sideMenu.preferredSize.width, height)
    centerComponent.peer.setBounds(0, 0, width, height)
    repaint()

  peer.addComponentListener(new java.awt.event.ComponentAdapter() {
    override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
      revalidate()
      repaint()
    }
  })
