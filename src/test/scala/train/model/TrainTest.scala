package train.model

import org.scalatest.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import train.model.Trains.Train
import train.model.Trains.TrainType.{HighSpeed, Normal}

class TrainTest extends AnyFunSuite:
  val REGIONAL_TRAIN_SPEED   = 130
  val HIGH_SPEED_TRAIN_SPEED = 300

  test("regionalTrainCreation"):
    val train: Train =
      Trains.makeRegionalTrain(name = "3905", carriages = 5, capacity = 400)
    train.maxSpeed should be(REGIONAL_TRAIN_SPEED)
    train.trainType should be(Normal)
    train.name should be("3905")
    train.carriages should be(5)
    train.capacity should be(400)

  test("highSpeedTrainCreation"):
    val train: Train =
      Trains.makeHighSpeedTrain(
        name = "Frecciarossa",
        carriages = 12,
        capacity = 900
      )
    train.maxSpeed should be(HIGH_SPEED_TRAIN_SPEED)
    train.trainType should be(HighSpeed)
    train.name should be("Frecciarossa")
    train.carriages should be(12)
    train.capacity should be(900)
