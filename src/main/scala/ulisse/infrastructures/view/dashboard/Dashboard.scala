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
  private val dashboardContent   = new JLayeredPane()
  private val defaultLayerLayout = StationSettings().stationEditorView

  private val sideMenu = SideMenu()
  sideMenu.background = Color.decode("#adc4db")

  dashboardContent.add(defaultLayerLayout.peer)
  dashboardContent.add(sideMenu.peer, JLayeredPane.PALETTE_LAYER)

  layout(Component.wrap(dashboardContent)) = BorderPanel.Position.Center

  peer.addComponentListener(new java.awt.event.ComponentAdapter() {
    override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
      val bounds = dashboardContent.getBounds()
      val width  = bounds.getWidth.toInt
      val height = bounds.getHeight.toInt
      sideMenu.peer.setBounds(0, 0, sideMenu.preferredSize.width, height)
      defaultLayerLayout.peer.setBounds(0, 0, width, height)
      repaint()
    }
  })

//GLASS PANE CONCEPT
//final case class Dashboard(root: UpdatableContainer) extends BorderPanel:
//  preferredSize = new Dimension(600, 400)
//  val layeredPane = new JLayeredPane()
//  val defaultComponent = StationSettings().stationEditorView
//  val paletteComponent = BorderPanel().opaque(false)
//
//  val sideMenu = SideMenu()
//  sideMenu.background = Color.decode("#adc4db")
//  paletteComponent.layout(sideMenu) = BorderPanel.Position.West
//  layeredPane.add(defaultComponent.peer)
//  layeredPane.add(sideMenu.peer, JLayeredPane.PALETTE_LAYER)
//
//  layout(Component.wrap(layeredPane)) = BorderPanel.Position.Center
//
//  peer.addComponentListener(new java.awt.event.ComponentAdapter() {
//    override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
//      val bounds = layeredPane.getBounds()
//      val width = bounds.getWidth.toInt
//      val height = bounds.getHeight.toInt
//      defaultComponent.peer.setBounds(0, 0, width, height)
//      paletteComponent.peer.setBounds(0, 0, width, height)
//      repaint()
//    }
//  })
