package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.manager.FormManager

import scala.swing.BorderPanel.Position

final case class EditorsView():
  private val mainPane = new ExtendedSwing.LayeredPanel()

  private val mapPark       = MapPanel.empty()
  private val menuPanel     = ExtendedSwing.JBorderPanelItem()
  private val mapController = FormManager.createMap()

  menuPanel.layout(mapController.component) = Position.East
  mainPane.add(mapPark)
  mainPane.add(menuPanel)
