package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.page.CentralController

import java.awt.BorderLayout
import scala.swing.BorderPanel.Position
import scala.swing.BorderPanel.Position.{Center, East}
import scala.swing.{BorderPanel, Component, Dimension}

final case class EditorsView():
  private val mainPane = new ExtendedSwing.LayeredPanel()

  private val mapPark       = MapPanel.empty()
  private val menuPanel     = ExtendedSwing.JBorderPanelItem()
  private val mapController = CentralController.createMap()

  menuPanel.layout(mapController.component) = Position.East
  mainPane.add(mapPark)
  mainPane.add(menuPanel)
