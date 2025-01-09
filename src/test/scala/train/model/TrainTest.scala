package train.model

import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.{must, should}
import train.model.Trains.{TechnologyType, Train}
import train.model.Trains.Carriages.{Carriage, UseType}

class TrainTest extends AnyFlatSpec:
  val technology: TechnologyType =
    TechnologyType(name = "HighSpeed", maxSpeed = 300)
  val carriageCapacity = 50
  val carriageCount    = 5
  val carriageInfo: Carriage =
    Carriage(UseType.Passenger, carriageCapacity)
  val train: Train =
    Trains.Train(
      name = "3905",
      techType = technology,
      carriage = carriageInfo,
      carriageCount = carriageCount
    )

  "A Train" should "provide overall capacity and length depending carriage specs" in:
    train.name should be("3905")
    train.carriage.use should be(UseType.Passenger)
    train.carriage.capacity should be(carriageCapacity)
    train.capacity should be(carriageCapacity * carriageCount)

  it should "have speed bounded by technology max speed" in:
    train.techType.name should be("HighSpeed")
    train.techType.maxSpeed should be(300)
    train.maxSpeed should be(train.techType.maxSpeed)
