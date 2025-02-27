package ulisse.entities.simulation

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.EnvironmentElements.TrainAgentEEWrapper.{findIn, leave}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

import scala.swing.Component

class EnvironmentElementsTest extends AnyWordSpec with Matchers:

  trait TestTrainAgentsContainer extends TrainAgentsContainer[TestTrainAgentsContainer]
  "agent containers" when:
    "created sequentially" should:
      val constructor = (id: Int) =>
        val mockedContainer = mock[TestTrainAgentsContainer]
        when(mockedContainer.id).thenReturn(id)
        mockedContainer
      "have sequential ids starting from 1" in:
        List(1, 2, 5, 10).foreach(id =>
          TrainAgentsContainer.generateSequentialContainers(constructor, id).zip(1 to id).foreach(
            (container, expectedId) => container.id shouldBe expectedId
          )
        )

      "be empty if the number of containers is not positive" in:
        List(-1, 0).foreach(id => TrainAgentsContainer.generateSequentialContainers(constructor, id) shouldBe List())

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = TrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private trait TestTrainAgentEEWrapper extends TrainAgentEEWrapper[TestTrainAgentEEWrapper]
  private val ee = mock[TestTrainAgentEEWrapper]

  "TrainAgent" should:
    "be able to leave a TrainAgentEEWrapper" in:
      when(ee.removeTrain(train3905)).thenReturn(Some(ee))
      train3905.leave(ee) shouldBe Some(ee)

    "not leave a TrainAgentEEWrapper if it's not present" in:
      when(ee.removeTrain(train3905)).thenReturn(None)
      train3905.leave(ee) shouldBe None

    "be find in a Seq of TrainAgentEEWrapper if present" in:
      val eeSeq = Seq(ee)
      when(ee.contains(train3905)).thenReturn(true)
      train3905.findIn(eeSeq) shouldBe Some(ee)

    "not be find in a Seq of TrainAgentEEWrapper if not present" in:
      val eeSeq = Seq(ee)
      when(ee.contains(train3905)).thenReturn(false)
      train3905.findIn(eeSeq) shouldBe None
