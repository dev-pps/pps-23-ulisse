package ulisse.adapters.output

import org.mockito.Mockito.verify
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.simulation.SimulationPage

class SimulationNotificationAdapterTest extends AnyWordSpec with Matchers:

  private val simulationData                = mock[SimulationData]
  private val mockedSimulationPage          = mock[SimulationPage]
  private val simulationNotificationAdapter = SimulationNotificationAdapter(mockedSimulationPage)

  "SimulationNotificationAdapter" should:
    "call updateData on SimulationPage when stepNotification is triggered" in:
      simulationNotificationAdapter.stepNotification(simulationData)
      verify(mockedSimulationPage).updateData(simulationData)

    "call endSimulation on SimulationPage when simulationEnded is triggered" in:
      simulationNotificationAdapter.simulationEnded(simulationData)
      verify(mockedSimulationPage).endSimulation(simulationData)
