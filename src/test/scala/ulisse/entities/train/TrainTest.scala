package ulisse.entities.train

import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrainTest extends AnyFlatSpec:
  val technology: Technology =
    Technology(name = "HighSpeed", maxSpeed = 300, acceleration = 1.0, deceleration = 0.5)
  val wagonCapacity = 50
  val length        = 5
  val wagonInfo: Wagon =
    Wagon(UseType.Passenger, wagonCapacity)
  val train: Train =
    Trains.Train(
      name = "3905",
      techType = technology,
      wagon = wagonInfo,
      length = length
    )

  "A Train" should "provide overall capacity and length depending wagon specs" in:
    train.name should be("3905")
    train.wagon.use should be(UseType.Passenger)
    train.wagon.capacity should be(wagonCapacity)
    train.capacity should be(wagonCapacity * length)

  it should "have speed bounded by technology max speed" in:
    train.techType.name should be("HighSpeed")
    train.techType.maxSpeed should be(300)
    train.maxSpeed should be(train.techType.maxSpeed)
