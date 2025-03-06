package ulisse.entities.timetable

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.{
  complexConfigurationData,
  trainAgents
}
import ulisse.entities.timetable.DynamicTimetableTest.dynamicTimetable1
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3907}
import ulisse.utils.Times.{ClockTime, Time}

class DynamicTimetableEnvironmentTest extends AnyWordSpec with Matchers:
  private val dynamicTimetableEnvironment = DynamicTimetableEnvironment(complexConfigurationData)
  "DynamicTimetableEnvironment" when:
    "created" should:
      "contains the configuration data timetables" in:
        dynamicTimetableEnvironment.dynamicTimetablesByTrain shouldBe complexConfigurationData.timetablesByTrain
        dynamicTimetableEnvironment.timetables shouldBe complexConfigurationData.timetables

    "queried for current timetable" should:
      "find the timetable if it exists" in:
        dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent3905) shouldBe Some(dynamicTimetable1)

      "return None if the timetable does not exist" in:
        dynamicTimetableEnvironment.findCurrentTimetableFor(trainAgent3907) shouldBe None

    "updated" should:
      "return a new DynamicTimetableEnvironment with the updated timetable" in:
        val departureTime = Time(1, 1, 1)
        dynamicTimetableEnvironment.updateTables(
          (dt, ct) => dt.departureUpdate(ct),
          _.nextRoute,
          trainAgent3905,
          departureTime
        ) match
          case Some((newEnvironment, nextRoute)) =>
            Some(nextRoute) shouldBe dynamicTimetable1.nextRoute
            newEnvironment.findCurrentTimetableFor(trainAgent3905) shouldBe dynamicTimetable1.departureUpdate(ClockTime(
              departureTime.h,
              departureTime.m
            ).getOrDefault)
          case _ => fail()

      "return None if the train is not in the environment" in:
        dynamicTimetableEnvironment.updateTables(
          (dt, ct) => dt.departureUpdate(ct),
          _.nextRoute,
          trainAgent3907,
          Time(1, 1, 1)
        ) shouldBe None

      "return None if fails to find route" in:
        dynamicTimetableEnvironment.updateTables(
          (dt, ct) => dt.departureUpdate(ct),
          _.currentRoute,
          trainAgent3905,
          Time(1, 1, 1)
        ) shouldBe None
