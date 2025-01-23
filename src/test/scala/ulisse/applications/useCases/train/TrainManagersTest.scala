package ulisse.applications.useCases.train

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.{be, not}
import org.scalatest.matchers.should.Matchers.should
import TrainManagers.TrainManager
import ulisse.applications.useCases.train.TrainManagers.TrainErrors.TrainAlreadyExists
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.UseType
import ulisse.entities.train.{Technology, Trains, Wagons}

class TrainManagersTest extends AnyFeatureSpec with GivenWhenThen:

  info("As Ulisse simulator user")
  info("I should be able to add, remove and edit trains")
  info("So I can create my trains fleet")
  info("and i can use them in my simulation")

  Feature("Managing saved trains (add, remove, update)"):
    val train = Trains.Train(
      "FR-200",
      Technology("HighSpeed", 300),
      Wagons.Wagon(UseType.Passenger, 300),
      4
    )
    Scenario("Save new train with no conflicts"):
      Given("that there are no train saved and at least one train technology is saved")
      val manager = TrainManager(List.empty)

      When("I ask the train-manager to add train")
      val res = manager.addTrain(train)
      Then("the train should be saved, no errors are returned and i got updated list of trains")
      res should be(Right(TrainManager(List(train))))

    Scenario("Save new train with a name of another (already saved) train"):
      Given("that there is one train saved and technology is assumed valid")
      val manager    = TrainManager(List(train))
      val technology = Technology("MonoRoute", 50)

      When("a train is added")
      val conflictName = train.name
      val sameNameTrain = Trains.Train(
        conflictName,
        technology,
        Wagons.Wagon(UseType.Other, 30),
        2
      )
      Then("an error should be returned so no train is added")
      val addResult = manager.addTrain(sameNameTrain)
      addResult should be(Left(TrainAlreadyExists(conflictName)))

    Scenario("Remove train"):
      Given("that there are one train (and its technology) saved")
      val manager = TrainManager(List(train))
      When("train is removed")
      val res = manager.removeTrain(train.name)
      Then("trains list should be empty")
      res should be(Right(TrainManager(List.empty)))

    Scenario("Update train information"):
      Given("that is train we wants update is saved")
      val initialTechnology = Technology("HighSpeed", 300)
      val newTechnology     = Technology("Normal", 130)
      val initialTrain = Trains.Train(
        "FR-200",
        initialTechnology,
        Wagons.Wagon(UseType.Passenger, 300),
        4
      )
      val manager = TrainManager(List(initialTrain))

      When("update train with new technology and wagon info")
      val res = manager.updateTrain(initialTrain.name)(
        newTechnology,
        Wagons.Wagon(UseType.Other, 50),
        7
      )

      Then("no errors should be returned")
      res should not be Left(TrainManagers.TrainErrors)

      Then("train should be updated correctly")
      val expectedTrain = Train(initialTrain.name, newTechnology, Wagons.Wagon(UseType.Other, 50), 7)
      res should be(Right(TrainManager(List(expectedTrain))))
