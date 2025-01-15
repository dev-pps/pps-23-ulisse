package applications.train

import applications.train.TrainManager.Errors
import applications.train.TrainManager.Errors.*
import entities.train.Trains.Train
import entities.train.Wagons.UseType
import entities.train.{Technology, Trains, Wagons}
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.{be, not}
import org.scalatest.matchers.should.Matchers.should

import scala.Right
import scala.language.postfixOps

class TrainServiceTest extends AnyFeatureSpec with GivenWhenThen:

  info("As Ulisse simulator user")
  info(
    "I want to be able to add, remove and edit trains and theirs type of technology"
  )
  info("So I can create my trains fleet")
  info("and i can use them in my simulation")

  Feature("Managing train technologies (add, remove)"):
    Scenario("Add new train technology"):
      val service = TrainManager.TrainService(List.empty)
      Given("there are no technologies saved yet")
      service.technologies.size should be(0)
      When("I add technology with a given name and max speed value")
      val technology = Technology("Standard", 130)
      val res        = service.addTechnology(technology)
      Then("technology should be saved")
      res should be(Right(List(Technology("Standard", 130))))

    Scenario("Try to add technology with already existing one (same name)"):
      val service = TrainManager.TrainService(List.empty)
      Given("some technologies are saved")
      service.addTechnology(Technology("Standard", 130))
      When("I add technology with an already saved technology name")
      val res = service.addTechnology(Technology("Standard", 130))
      Then("technology should not be saved")
      res should be(Left(TechnologyAlreadyExists("Standard")))

    Scenario("Try to remove technology"):
      val service = TrainManager.TrainService(List.empty)
      Given("there is one technologies saved")
      service.addTechnology(Technology("Standard", 130))
      service.technologies.size should be(1)
      When("I remove technology")
      val res = service.removeTechnology(name = "Standard")
      Then("there are no technologies saved (technology list is empty)")
      res match
        case Left(e)   => fail(s"removing technology failed due to: $e")
        case Right(tl) => tl.size should be(0)

  Feature("Managing saved trains (add, remove, update)"):
    val train = Trains.Train(
      "FR-200",
      Technology("HighSpeed", 300),
      Wagons.Wagon(UseType.Passenger, 300),
      4
    )
    Scenario("Save new train with no conflicts"):
      Given(
        "that there are no train saved and at least one train technology is saved"
      )
      val service = TrainManager.TrainService(List.empty)
      service.addTechnology(Technology("HighSpeed", 300)) should be(
        Right(List(Technology("HighSpeed", 300)))
      )

      When("I ask the train-service to add train")
      val res = service.addTrain(train)
      Then(
        "the train should be saved, no errors are returned and i got updated list of trains"
      )
      res match
        case Right(List(Train(n, _, _, _))) => assert(n.contentEquals("FR-200"))
        case Left(e)  => fail(s"During train creation an error occurred: $e")
        case Right(r) => fail(s"Not expected return of add methods: $r")

    Scenario("Save new train with a name of another (already saved) train"):
      Given("that there is one train saved and also its related technology")
      val service = TrainManager.TrainService(List.empty)
      val tech    = Technology("MonoRoute", 50)
      service.addTechnology(train.techType) should be(
        Right(List(train.techType))
      )
      service.addTechnology(tech) should be(Right(List(train.techType, tech)))
      service.addTrain(train) should not be (Left(Errors))
      When("I click add button")
      val conflictName = train.name
      val sameNameTrain = Trains.Train(
        conflictName,
        tech,
        Wagons.Wagon(UseType.Other, 30),
        2
      )
      Then("an error should be returned")
      service.addTrain(sameNameTrain) should be(
        Left(TrainAlreadyExists(conflictName))
      )
      Then("train should not be added (train amount must be 1)")
      service.trains.size should be(1)

    Scenario("Remove train"):
      Given("that there are one train (and its technology) saved")
      val technology = Technology("Normal", 130)
      val service    = TrainManager.TrainService(List(technology))
      service.addTechnology(technology)
      val train = Train(
        "RE-8089",
        technology,
        Wagons.Wagon(UseType.Passenger, 20),
        6
      )
      service.addTrain(train) should not be Left(Errors)
      When("I removed train")
      val res = service.removeTrain(train.name)
      Then("trains list should be empty")
      res should be(Right(List.empty))

    Scenario("Update train information"):
      Given("that is train we wants update is saved")
      val initialTechnology = Technology("HighSpeed", 300)
      val newTechnology     = Technology("Normal", 130)
      val service           = TrainManager.TrainService(List.empty)
      List(initialTechnology, newTechnology).foreach(service.addTechnology)
      val initTrain = Trains.Train(
        "FR-200",
        Technology("HighSpeed", 300),
        Wagons.Wagon(UseType.Passenger, 300),
        4
      )
      service.addTrain(initTrain)

      When("update train with new technology and wagon info")
      val res = service.updateTrain(initTrain.name)(
        newTechnology,
        Wagons.Wagon(UseType.Other, 50),
        7
      )

      Then("no errors should be returned")
      res should not be Left(Errors)

      Then("train should be updated correctly")
      res match
        case Right(List(Train(n, tk, w, c))) =>
          n should be(initTrain.name)
          tk.name should be(newTechnology.name)
          tk.maxSpeed should be(newTechnology.maxSpeed)
          w.use should be(UseType.Other)
          w.capacity should be(50)
          c should be(7)
        case Left(e)  => fail(s"Fail: ${e.description}")
        case Right(l) => fail(s"Right excepted: $l")
