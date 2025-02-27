package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.manager.FormManager

import java.awt.BorderLayout
import javax.swing.JLayeredPane
import scala.swing.BorderPanel.Position
import scala.swing.BorderPanel.Position.{Center, East}
import scala.swing.{BorderPanel, Component, Dimension}

final case class EditorsView():
  private val mainPane = new ExtendedSwing.LayeredPanel()

  private val mapPark       = MapPanel.empty()
  private val menuPanel     = ExtendedSwing.JBorderPanelItem()
  private val mapController = FormManager.createMap()

  menuPanel.layout(mapController.component) = Position.East
  mainPane.add(mapPark, JLayeredPane.DEFAULT_LAYER)
  mainPane.add(menuPanel, JLayeredPane.PALETTE_LAYER)
