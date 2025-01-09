package train.model

import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import train.model.Trains.{Technology, Train}
import train.model.Trains.Carriages.{Carriage, UseType}

class TrainTest extends AnyFlatSpec:

  "A Train" should "provide overall capacity and length of train by carriage info" in:
    val carriageCapacity = 50
    val carriageCount    = 5
    val carriageInfo =
      Carriage(UseType.Passenger, carriageCapacity)
    val train: Train =
      Trains.Train(
        name = "3905",
        techType = Technology(name = "HighSpeed", maxSpeed = 300),
        carriage = carriageInfo,
        carriageCount = carriageCount
      )
    train.name should be("3905")
    train.techType.name should be ("HighSpeed")
    train.techType.maxSpeed should be (300)
    train.maxSpeed should be (train.techType.maxSpeed)
    train.carriage.use should be (UseType.Passenger)
    train.carriage.capacity should be (carriageCapacity)
    train.capacity should be (carriageCapacity * carriageCount)

//  test("highSpeedTrainCreation"):
//    val train: Train =
//      Trains.makeHighSpeedTrain(
//        name = "Frecciarossa",
//        carriages = 12,
//        capacity = 900
//      )
//    train.maxSpeed should be(HIGH_SPEED_TRAIN_SPEED)
//    train.trainType should be(HighSpeed)
//    train.name should be("Frecciarossa")
//    train.carriages should be(12)
//    train.capacity should be(900)
