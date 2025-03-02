package ulisse.entities.train

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.TrainAgentTest.{train3905, trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

object TrainAgentTest:
  val defaultTechnology = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  val defaultWagon = Wagon(UseType.Passenger, 50)
  val defaultWagonNumber = 5
  val train3905 = makeTrain("3905")
  val train3906 = makeTrain("3906")
  val train3907 = makeTrain("3907")
  val trainAgent3905 = makeTrainAgent(train3905)
  val trainAgent3906 = makeTrainAgent(train3906)
  val trainAgent3907 = makeTrainAgent(train3907)
  
  private def makeTrain(name: String): Train =
    Train(name, defaultTechnology, defaultWagon, defaultWagonNumber)

  private def makeTrainAgent(train: Train): TrainAgent =
    TrainAgent(train)
    
class TrainAgentTest extends AnyWordSpec with Matchers:

  "TrainAgent" when:
    "created" should:
      "have the same train info" in:
        trainAgent3905.name shouldBe train3905.name
        trainAgent3905.techType shouldBe train3905.techType
        trainAgent3905.wagon shouldBe train3905.wagon
        trainAgent3905.length shouldBe train3905.length
        trainAgent3905.lengthSize shouldBe train3905.lengthSize
        trainAgent3905.maxSpeed shouldBe train3905.maxSpeed
        trainAgent3905.capacity shouldBe train3905.capacity

      "have a distance travelled of 0" in:
        trainAgent3905.distanceTravelled shouldBe 0

    "distance is updated" should:
      "be updated correctly" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        updatedTrainAgent3905.distanceTravelled shouldBe 10
        updatedTrainAgent3905.updateDistanceTravelled(5).distanceTravelled shouldBe 15

      "be set correctly" in:
        val updatedTrainAgent3905 = trainAgent3905.distanceTravelled = 10
        updatedTrainAgent3905.distanceTravelled shouldBe 10
        (updatedTrainAgent3905.distanceTravelled = 5).distanceTravelled shouldBe 5

      "be set to 0 when reset" in:
        trainAgent3905.updateDistanceTravelled(20).resetDistanceTravelled().distanceTravelled shouldBe 0

      "be at least 0" in:
        (trainAgent3905.distanceTravelled = -10).distanceTravelled shouldBe 0
        trainAgent3905.updateDistanceTravelled(10).updateDistanceTravelled(-15).distanceTravelled shouldBe 0
