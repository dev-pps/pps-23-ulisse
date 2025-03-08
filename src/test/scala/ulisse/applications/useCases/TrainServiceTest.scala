package ulisse.applications.useCases

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Runner.runAll
import ulisse.Utils.MatchersUtils.should
import ulisse.applications.managers.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.{AppState, EventQueue}
import ulisse.applications.managers.TrainManagers.{TrainErrors, TrainManager}
import ulisse.entities.TestMockedEntities.{AV1000Train, AV800Train}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TrainServiceTest extends AnyWordSpec with Matchers with BeforeAndAfterEach:
  private val initialSavedTrains = List(AV1000Train, AV800Train)
  private val AVTech             = TrainTechnology("AV", 300, 1, 1)
  private val normalTech         = TrainTechnology("Normal", 160, 1, 1)

  private val technologyManager = TechnologyManager[TrainTechnology](List(AVTech, normalTech))
  private val trainManager      = TrainManager(initialSavedTrains)
  private val initialState =
    AppState().updateTechnology(_ => technologyManager).updateTrain((_, _, tm) => (trainManager, tm))
  private val eventQueue    = EventQueue()
  private val inputPort     = TrainService(eventQueue)
  private def updateState() = runAll(initialState, eventQueue.events)

  "TrainService" should:
    "remove saved train by train name" in:
      val trainName     = AV1000Train.name
      val requestResult = inputPort.removeTrain(trainName)
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case Left(e)              => fail(s"train not deleted cause: $e")
        case Right(updatedTrains) => updatedTrains shouldBe List(AV800Train)
        case _                    => fail(s"wrong return type")

    "returns saved trains" in:
      val requestResult = inputPort.trains
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case actualTrains => actualTrains shouldBe initialSavedTrains
        case _            => fail(s"wrong return type")

    "creates train in case of no duplicates or errors" in:
      val trainLength   = 12
      val wagonInfo     = Wagons.PassengerWagon(300)
      val expectedTrain = Train("newTrain", AVTech, wagonInfo, trainLength)
      val requestResult = inputPort.createTrain(
        name = "newTrain",
        technologyName = AVTech.name,
        wagonUseTypeName = wagonInfo.use.name,
        wagonCapacity = wagonInfo.capacity,
        wagonCount = trainLength
      )
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case Right(updatedTrains) => updatedTrains.contains(expectedTrain) shouldBe true
        case Left(error)          => fail(s"train not saved cause: $error")

    "returns error if train name is same of some other already saved " in:
      val trainLength = 12
      val wagonInfo   = Wagons.PassengerWagon(300)
      val requestResult = inputPort.createTrain(
        name = AV800Train.name,
        technologyName = AVTech.name,
        wagonUseTypeName = wagonInfo.use.name,
        wagonCapacity = wagonInfo.capacity,
        wagonCount = trainLength
      )
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case Right(_)          => fail(s"wrong return type")
        case Left(actualError) => actualError shouldBe TrainErrors.TrainAlreadyExists(AV800Train.name)

    "returns error if any technology if found with given technology name" in:
      val notExistTechnologyName = "Magnetic"
      val trainLength            = 12
      val wagonInfo              = Wagons.PassengerWagon(300)
      val requestResult = inputPort.createTrain(
        name = AV800Train.name,
        technologyName = notExistTechnologyName,
        wagonUseTypeName = wagonInfo.use.name,
        wagonCapacity = wagonInfo.capacity,
        wagonCount = trainLength
      )
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case Right(_)          => fail(s"wrong return type")
        case Left(actualError) => actualError shouldBe TechnologyNotExists(notExistTechnologyName)

    "updates train given its name and valid new info" in:
      val newWagon      = Wagons.OtherWagon(200)
      val newLength     = AV800Train.length + 30
      val expectedTrain = Train(AV800Train.name, normalTech, newWagon, newLength)
      val requestResult = inputPort.updateTrain(name = AV800Train.name)(
        technologyName = normalTech.name,
        wagonUseTypeName = newWagon.use.name,
        wagonCapacity = newWagon.capacity,
        wagonCount = newLength
      )
      updateState()
      Await.result(requestResult, Duration.Inf) match
        case Right(updatedTrains) => updatedTrains.contains(expectedTrain) shouldBe true
        case Left(error)          => fail(s"train not updated cause: $error")
