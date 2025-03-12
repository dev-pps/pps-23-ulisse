package ulisse.entities.train

import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrainTest extends AnyFlatSpec:
  val technology: Trains.TrainTechnology =
    TrainTechnology(name = "HighSpeed", maxSpeed = 300, acceleration = 1.0, deceleration = 0.5)
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

  "unapply" should "provide train name, technology, wagon info and length" in:
    train match
      case Trains.Train(name, techType, wagon, length) =>
        name should be("3905")
        techType should be(technology)
        wagon should be(wagonInfo)
        length should be(5)
      case _ => fail("Train unapply failed")

  "A Train" should "provide overall capacity and length depending wagon specs" in:
    train.name should be("3905")
    train.wagon.use should be(UseType.Passenger)
    train.wagon.capacity should be(wagonCapacity)
    train.capacity should be(wagonCapacity * length)

  it should "have speed bounded by technology max speed" in:
    train.techType.name should be("HighSpeed")
    train.techType.maxSpeed should be(300)
    train.maxSpeed should be(train.techType.maxSpeed)

  it should "provide how many meters train is long" in:
    val expectedMeterSize = length * Wagons.UseType.Passenger.lengthSize
    train.lengthSize should be(expectedMeterSize)

  it should "be different if not a train" in:
    train equals "not a train" should be(false)
