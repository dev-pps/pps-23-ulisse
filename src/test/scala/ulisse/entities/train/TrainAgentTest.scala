package ulisse.entities.train

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrainAgentTest extends AnyWordSpec with Matchers:

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train              = Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val trainAgent         = TrainAgent.apply(train)

  "TrainAgent" when:
    "created" should:
      "have a distance travelled of 0" in:
        trainAgent.distanceTravelled shouldBe 0

    "distance is updated" should:
      "be updated correctly" in:
        val updatedTrainAgent = trainAgent.updateDistanceTravelled(10)
        updatedTrainAgent.distanceTravelled shouldBe 10
        updatedTrainAgent.updateDistanceTravelled(5).distanceTravelled shouldBe 15

      "be set correctly" in:
        val updatedTrainAgent = trainAgent.distanceTravelled = 10
        updatedTrainAgent.distanceTravelled shouldBe 10
        (updatedTrainAgent.distanceTravelled = 5).distanceTravelled shouldBe 5

      "be set to 0 when reset" in:
        trainAgent.updateDistanceTravelled(20).resetDistanceTravelled().distanceTravelled shouldBe 0

      "be at least 0" in:
        (trainAgent.distanceTravelled = -10).distanceTravelled shouldBe 0
        trainAgent.updateDistanceTravelled(10).updateDistanceTravelled(-15).distanceTravelled shouldBe 0

    "on route" should:
      "be true when distance travelled is greater than 0" in:
        trainAgent.updateDistanceTravelled(1).isOnRoute shouldBe true

      "be false when distance travelled is 0" in:
        trainAgent.isOnRoute shouldBe false
