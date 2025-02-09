package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.LayeredContainers.JLayeredPane
import ulisse.infrastructures.view.form.CentralController
import ulisse.infrastructures.view.menu.Menu

import java.awt.BorderLayout
import scala.swing.{BorderPanel, Component, Dimension}
import scala.swing.BorderPanel.Position.{Center, East}

final case class EditorsView() extends JLayeredPane:
  private val mapPark: MapPanel = MapPanel.empty()
  private val mapController     = CentralController.createMap()

  mainPane.peer.add(mapPark.peer)
  glassPane.peer.add(mapController.component.peer, BorderLayout.EAST)
