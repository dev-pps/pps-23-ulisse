package ulisse.applications.train

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import ulisse.entities.train.Technology
import ulisse.applications.useCases.train.TechnologyManagers.TechnologyManager
import ulisse.applications.useCases.train.TechnologyManagers.TechErrors.TechnologyAlreadyExists
import ulisse.applications.useCases.train.TechnologyManagers.TechErrors.InvalidSpeed

class TechnologyManagerTest extends AnyFeatureSpec with GivenWhenThen:
  val emptyManager: TechnologyManager = TechnologyManager(List.empty)
  val technology: Technology          = Technology("Standard", 130)

  Feature("Managing train technologies (add, remove)"):
    Scenario("Add new train technology"):
      Given("there are no technologies saved yet")
      emptyManager.technologiesList.size should be(0)
      When("I add technology with a given name and max speed value")
      val res = emptyManager.add(technology)
      Then("technology should be saved")
      res should be(Right(TechnologyManager(List(technology))))

    Scenario("Try to add technology with already existing one (same name)"):
      Given("some technologies are saved")
      val technology = Technology("Standard", 130)
      When("I add technology twice (with same name)")
      val res =
        for
          m <- emptyManager.add(technology)
          r <- m.add(technology)
        yield r
      Then("technology should not be saved and error returned")
      res should be(Left(TechnologyAlreadyExists("Standard")))

    Scenario("Try to remove technology"):
      Given("there is one technologies saved")
      When("I remove technology")
      val res =
        for
          m <- emptyManager.add(technology)
          r <- m.remove(technology.name)
        yield r
      Then("there are no technologies saved (technology list is empty)")
      res should be(Right(TechnologyManager(List.empty)))

    Scenario("Add new technology with invalid value of max speed"):
      Given("that no technologies are saved")
      When("I add new technology with negative max speed")
      val negativeSpeed     = -100
      val invalidTechnology = Technology("TechNotValid", negativeSpeed)
      val res               = emptyManager.add(invalidTechnology)
      Then("it should be returned an InvalidValue error")
      res should be(Left(InvalidSpeed(negativeSpeed)))
