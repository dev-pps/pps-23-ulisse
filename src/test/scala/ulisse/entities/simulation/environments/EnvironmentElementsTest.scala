package ulisse.entities.simulation.environments

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.{findIn, leave}
import ulisse.entities.simulation.environments.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.train.TrainAgents.TrainAgent

class EnvironmentElementsTest extends AnyWordSpec with Matchers:
  trait TestTrainAgentsContainer extends TrainAgentsContainer[TestTrainAgentsContainer]

  "TrainAgentsContainer" when:
    "created sequentially" should:
      val constructor = (id: Int) =>
        val mockedContainer = mock[TestTrainAgentsContainer]
        when(mockedContainer.id).thenReturn(id)
        mockedContainer
      "have sequential id's starting from 1" in:
        List(1, 2, 5, 10).foreach(id =>
          TrainAgentsContainer.generateSequentialContainers(constructor, id).zip(1 to id).foreach(
            (container, expectedId) => container.id shouldBe expectedId
          )
        )

      "be not created if the number of desired containers is not positive" in:
        List(-1, 0).foreach(id => TrainAgentsContainer.generateSequentialContainers(constructor, id) shouldBe List())

  private trait TestTrainAgentEEWrapper extends TrainAgentEEWrapper[TestTrainAgentEEWrapper]
  private val train      = mock[TrainAgent]
  private val otherTrain = mock[TrainAgent]
  private val ee         = mock[TestTrainAgentEEWrapper]
  private val ee2        = mock[TestTrainAgentEEWrapper]
  private val tac        = mock[TestTrainAgentsContainer]
  private val tac2       = mock[TestTrainAgentsContainer]

  "TrainAgent" should:
    "be able to leave a TrainAgentEEWrapper" in:
      when(ee.removeTrain(train)).thenReturn(Some(ee))
      train.leave(ee) shouldBe Some(ee)

    "not leave a TrainAgentEEWrapper if it's not present" in:
      when(ee.removeTrain(train)).thenReturn(None)
      train.leave(ee) shouldBe None

    "be find in a Seq of TrainAgentEEWrapper if present" in:
      when(ee.contains(train)).thenReturn(true)
      train.findIn(Seq(ee)) shouldBe Some(ee)

    "not be find in a Seq of TrainAgentEEWrapper if not present" in:
      when(ee.contains(train)).thenReturn(false)
      train.findIn(Seq(ee)) shouldBe None

    "find all train in the environment" in:
      when(tac.trains).thenReturn(Seq(train))
      when(tac2.trains).thenReturn(Seq(otherTrain))
      when(ee.containers).thenReturn(Seq(tac, tac2))
      ee.trains shouldBe Seq(train, otherTrain)

    "return empty list if the environment hasn't containers" in:
      when(ee.containers).thenReturn(Seq())
      ee.trains shouldBe Seq()

    "return empty list if the environment hasn't trains" in:
      when(tac.trains).thenReturn(Seq())
      when(tac2.trains).thenReturn(Seq())
      when(ee.containers).thenReturn(Seq(tac, tac2))
      ee.trains shouldBe Seq()

    "find all train in a Seq of environments" in:
      when(tac.trains).thenReturn(Seq(train))
      when(tac2.trains).thenReturn(Seq(otherTrain))
      when(ee.containers).thenReturn(Seq(tac))
      when(ee2.containers).thenReturn(Seq(tac2))
      Seq(ee, ee2).collectTrains shouldBe Seq(train, otherTrain)

    "return empty list if the Seq of environments hasn't containers" in:
      when(ee.containers).thenReturn(Seq())
      when(ee2.containers).thenReturn(Seq())
      Seq(ee, ee2).collectTrains shouldBe Seq()

    "return empty list if the Seq of environments hasn't trains" in:
      when(tac.trains).thenReturn(Seq())
      when(tac2.trains).thenReturn(Seq())
      when(ee.containers).thenReturn(Seq(tac))
      when(ee2.containers).thenReturn(Seq(tac2))
      Seq(ee, ee2).collectTrains shouldBe Seq()
