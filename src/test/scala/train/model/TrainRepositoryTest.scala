package train.model

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import train.model.TrainRepositories.Errors
import train.model.Trains.Carriages.UseType.{Other, Passenger}
import train.model.Trains.{Carriages, TechnologyType, Train}

class TrainRepositoryTest extends AnyFeatureSpec with GivenWhenThen:

  info("As Ulisse simulator user")
  info("I want to be able to add, remove and edit trains")
  info("So I can create my trains fleet")
  info("and i can use them in my simulation")

  Feature("Creation of new train"):
    val train = Trains.Train(
      "FR-200",
      TechnologyType("HighSpeed", 300),
      Carriages.Carriage(Passenger, 300),
      4
    )
    Scenario("Fill all train fields and save edits"):
      Given("that there are no train saved")
      val trainRepo = TrainRepositories.TrainRepository
      When("I fill all fields and I click add button")
      val res = trainRepo.add(train)
      Then("the train should be saved")
      res match
        case Right(Train(n, _, _, _)) => assert(n.contentEquals("FR-200"))
        case Left(e)  => fail(s"During train creation an error occurred: $e")
        case Right(r) => fail(s"Not expected return of add methods: $r")

    Scenario("Fill name fields with an already existing train name"):
      Given("that there is one train saved")
      val trainRepo = TrainRepositories.TrainRepository
      trainRepo.add(train)
      When("I click add button")
      val sameNameTrain = Trains.Train(
        train.name,
        TechnologyType("MonoRoute", 50),
        Carriages.Carriage(Other, 30),
        2
      )
      val res = trainRepo.add(sameNameTrain)
      Then("an error should be returned")
      res match
        case Left(e) => e.description should be("Train already exist")
        case _ => fail(s"Add train with existing train name must not possible")
      Then("train should not be added (train amount must be 1)")
      trainRepo.trains.size should be(1)

    Scenario("Update train information"):
      Given("that there is train we wants update")
      val trainRepo = TrainRepositories.TrainRepository
      trainRepo.add(train)
      When("I edit train technology and I click update button")
      val res = trainRepo.update(train.name)(
        TechnologyType("MonoRoute", 30),
        Carriages.Carriage(Other, 50),
        7
      )
      Then("edits should be saved")
      res match
        case Right(Train(name, technology, carriage, carriageCount)) =>
          println(s"$name $technology $carriageCount")
          name should be("FR-200")
          technology.name should be("MonoRoute")
          technology.maxSpeed should be(30)
          carriage.use should be(Other)
          carriage.capacity should be(50)
          carriageCount should be(7)
        case Left(err) => fail(err.description)
        case Right(t)  => fail(s"returned $t instead Train")
      Then("the trains saved not changed")
      trainRepo.trains.size should be(1)

    Scenario("Remove train"):
      Given("that there are two train saved with names RE-8089 and FR-200")
      val trainRepo = TrainRepositories.TrainRepository
      trainRepo.add(train)
      trainRepo.add(Train(
        "RE-8089",
        TechnologyType("Normal", 130),
        Carriages.Carriage(Passenger, 20),
        6
      ))
      trainRepo.trains.size should be(2)

      When("I removed train FR-200")
      val res = trainRepo.remove(train.name)
      Then("there should be only train RE-8089")
      res match
        case Left(e) => fail(s"removing train failed due to: $e")
        case Right(tl) =>
          tl.size should be(1)
          tl.map(_.name).headOption should be(Some("RE-8089"))
