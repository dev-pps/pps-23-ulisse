package train

import org.scalatest.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.{must, should}
import must.Matchers.be
import should.Matchers.should
import train.Trains.Train
import train.Trains.TrainType.NORMAL

class TrainTest extends AnyFunSuite:

  test("trainCreation"):
    val train: Train = Trains.RegionalTrain(name = "3905", carriages = 5, capacity = 400)
    train.maxSpeed should be (130)
    train.trainType should be (NORMAL)
    train.name should be ("3905")
    train.carriages should be (5)
    train.capacity should be (400)

