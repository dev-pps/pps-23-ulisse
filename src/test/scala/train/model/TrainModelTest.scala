package train.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import train.model.TrainModels.Errors
import train.model.TrainModels.Errors.TechnologyAlreadyExists
import train.model.domain.{Trains, Wagons}
import train.model.domain.Wagons.UseType
import train.model.domain.Technology
import train.model.domain.Trains.Train

class TrainModelTest extends AnyFeatureSpec with GivenWhenThen:

  info("As Ulisse simulator user")
  info(
    "I want to be able to add, remove and edit trains and theirs type of technology"
  )
  info("So I can create my trains fleet")
  info("and i can use them in my simulation")

  Feature("Managing saved trains (add, remove, update)"):
    val train = Trains.Train(
      "FR-200",
      Technology("HighSpeed", 300),
      Wagons.Wagon(UseType.Passenger, 300),
      4
    )
    Scenario("Fill all train fields and save edits"):
      Given("that there are no train saved")
      val trainRepo = TrainModels.TrainModel
      When("I fill all fields and I click add button")
      val res = trainRepo.add(train)
      Then("the train should be saved")
      res match
        case Right(Train(n, _, _, _)) => assert(n.contentEquals("FR-200"))
        case Left(e)  => fail(s"During train creation an error occurred: $e")
        case Right(r) => fail(s"Not expected return of add methods: $r")

    Scenario("Fill name fields with an already existing train name"):
      Given("that there is one train saved")
      val model = TrainModels.TrainModel
      model.add(train)
      When("I click add button")
      val sameNameTrain = Trains.Train(
        train.name,
        Technology("MonoRoute", 50),
        Wagons.Wagon(UseType.Other, 30),
        2
      )
      val res = model.add(sameNameTrain)
      Then("an error should be returned")
      res match
        case Left(e) => e.description should be("Train already exist")
        case _ => fail(s"Add train with existing train name must not possible")
      Then("train should not be added (train amount must be 1)")
      model.trains.size should be(1)

    Scenario("Update train information"):
      Given("that there is train we wants update")
      val model = TrainModels.TrainModel
      model.add(train)
      When("I edit train info and I click update button")
      val res = model.update(train.name)(
        Technology("MonoRoute", 30),
        Wagons.Wagon(UseType.Other, 50),
        7
      )
      Then("edits should be saved")
      res match
        case Right(Train(name, technology, carriage, carriageCount)) =>
          println(s"$name $technology $carriageCount")
          name should be("FR-200")
          technology.name should be("MonoRoute")
          technology.maxSpeed should be(30)
          carriage.use should be(UseType.Other)
          carriage.capacity should be(50)
          carriageCount should be(7)
        case Left(err) => fail(err.description)
        case Right(t)  => fail(s"returned $t instead Train")
      Then("the trains saved not changed")
      model.trains.size should be(1)

    Scenario("Remove train"):
      Given("that there are two train saved with names RE-8089 and FR-200")
      val model = TrainModels.TrainModel
      model.add(train)
      model.add(Train(
        "RE-8089",
        Technology("Normal", 130),
        Wagons.Wagon(UseType.Passenger, 20),
        6
      ))
      model.trains.size should be(2)

      When("I removed train FR-200")
      val res = model.remove(train.name)
      Then("there should be only train RE-8089")
      res match
        case Left(e) => fail(s"removing train failed due to: $e")
        case Right(tl) =>
          tl.size should be(1)
          tl.map(_.name).headOption should be(Some("RE-8089"))

  Feature("Managing train technologies (add, remove)"):
    Scenario("Add new train technology"):
      val domain = TrainModels.TrainModel
      Given("there are no technologies saved yet")
      domain.technologies.size should be(0)
      When("I add technology with a given name and max speed value")
      val technology = Technology("Standard", 130)
      val res        = domain.addTechnology(technology)
      Then("technology should be saved")
      res should be(Right[Errors, Technology](Technology("Standard", 130)))

    Scenario("Try to add technology with already existing one (same name)"):
      val domain = TrainModels.TrainModel
      Given("some technologies are saved")
      domain.addTechnology(Technology("Standard", 130))
      When("I add technology with an already saved technology name")
      val res = domain.addTechnology(Technology("Standard", 130))
      Then("technology should not be saved")
      res should be(
        Left[Errors, Technology](TechnologyAlreadyExists("Standard"))
      )

    Scenario("Try to remove technology"):
      val domain = TrainModels.TrainModel
      Given("there is one technologies saved")
      domain.addTechnology(Technology("Standard", 130))
      domain.technologies.size should be(1)
      When("I remove technology")
      val res = domain.removeTechnology(name = "Standard")
      Then("there are no technologies saved (technology list is empty)")
      res match
        case Left(e)   => fail(s"removing technology failed due to: $e")
        case Right(tl) => tl.size should be(0)
