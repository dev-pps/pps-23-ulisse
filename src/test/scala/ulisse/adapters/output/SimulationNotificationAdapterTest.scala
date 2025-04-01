package ulisse.adapters.output

import org.mockito.Mockito.verify
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.data.SimulationData

class SimulationNotificationAdapterTest extends AnyWordSpec with Matchers:
  private val simulationData            = mock[SimulationData]
  private val mockedSimulationListener  = mock[SimulationNotificationListener]
  private val otherMockedSimulationPage = mock[SimulationNotificationListener]
  private val simulationNotificationAdapter =
    SimulationNotificationAdapter(mockedSimulationListener, otherMockedSimulationPage)

  "SimulationNotificationAdapter" should:
    "call updateData on every SimulationListener when stepNotification is triggered" in:
      simulationNotificationAdapter.stepNotification(simulationData)
      verify(mockedSimulationListener).updateData(simulationData)
      verify(otherMockedSimulationPage).updateData(simulationData)

    "call endSimulation on every SimulationListener when simulationEnded is triggered" in:
      simulationNotificationAdapter.simulationEnded(simulationData)
      verify(mockedSimulationListener).endSimulation(simulationData)
      verify(otherMockedSimulationPage).endSimulation(simulationData)
