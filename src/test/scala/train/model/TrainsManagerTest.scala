package train.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.*
import org.scalatest.matchers.should.Matchers.should

class TrainsManagerTest extends AnyFlatSpec:
  val name                        = "R-8086"
  val trainType: Trains.TrainType = Trains.TrainType.Normal
  val carriages                   = 3
  val capacity                    = 300
  val tm: TrainsManager.type      = TrainsManager

  "A TrainsManager" should "create new train given name, type, carriages and capacity" in:
    tm.createTrain(name, trainType, carriages, capacity)
    val addedTrain = tm.listTrains.find(_.name.contentEquals(name))
    addedTrain.map(t =>
      t.name should be(name)
      t.trainType should be(trainType)
      t.carriages should be(carriages)
      t.capacity should be(capacity)
    )

  it should "produce IllegalArgumentException when is created new train with already existing name train" in {
    an[IllegalArgumentException] should be thrownBy tm.createTrain(
      name,
      trainType,
      carriages,
      capacity
    )
  }

  "A TrainsManager with some trains saved" should "delete train given its name" in:
    val trainName = "R-3033"
    tm.addTrain(Trains.makeRegionalTrain(trainName, 4, 500))
    tm.delete(trainName)
    tm.listTrains.map(_.name) should not contain "R-3033"

  it should "update train information given train name" in:
    val newTrainName = "R-2220"
    tm.createTrain(newTrainName, trainType, carriages, capacity)
    tm.update(newTrainName)(
      trainType = Trains.TrainType.HighSpeed,
      carriages = 1,
      capacity
    )
    tm.listTrains.find(_.name.contentEquals(newTrainName)).map(t =>
      t.name should be(newTrainName)
      t.trainType should be(Trains.TrainType.HighSpeed)
      t.carriages should be(1)
      t.capacity should be(capacity)
    )
