package train.model

import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import train.model.domain.Trains.Train
import train.model.domain.Wagons.{UseType, Wagon}
import train.model.domain.Technology
import train.model.domain.Trains

class TrainTest extends AnyFlatSpec:
  val technology: Technology =
    Technology(name = "HighSpeed", maxSpeed = 300)
  val wagonCapacity = 50
  val wagonCount    = 5
  val wagonInfo: Wagon =
    Wagon(UseType.Passenger, wagonCapacity)
  val train: Train =
    Trains.Train(
      name = "3905",
      techType = technology,
      wagon = wagonInfo,
      wagonCount = wagonCount
    )

  "A Train" should "provide overall capacity and length depending wagon specs" in:
    train.name should be("3905")
    train.wagon.use should be(UseType.Passenger)
    train.wagon.capacity should be(wagonCapacity)
    train.capacity should be(wagonCapacity * wagonCount)

  it should "have speed bounded by technology max speed" in:
    train.techType.name should be("HighSpeed")
    train.techType.maxSpeed should be(300)
    train.maxSpeed should be(train.techType.maxSpeed)
