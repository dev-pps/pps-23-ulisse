package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.page.forms.Form

import scala.swing.BorderPanel.Position

final case class EditorsView():
  private val mainPane = new ExtendedSwing.SLayeredPanel()

  private val mapPark   = MapPanel.empty()
  private val menuPanel = ExtendedSwing.SBorderPanel()

  private val stationForm   = Form.createStation()
  private val routeForm     = Form.createRoute()
  private val scheduleForm  = Form.createSchedule()
  private val mapController = FormManager.createMap(stationForm, routeForm, scheduleForm)

  menuPanel.layout(mapController.component) = Position.East
  mainPane.add(mapPark)
  mainPane.add(menuPanel)
