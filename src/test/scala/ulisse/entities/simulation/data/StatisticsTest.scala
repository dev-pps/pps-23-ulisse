package ulisse.entities.simulation.data

import cats.Id
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.timetable.DynamicTimetableTest.{dynamicTimetable1, dynamicTimetable2}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgentTest.trainAgent3905
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.entities.simulation.data.Statistics.*

class StatisticsTest extends AnyWordSpec with Matchers:

  "Statistics for TrainAgentInfo" when:
    "currentDynamicTimetable is requested" should:
      "find it if present" in:
        val delay = ClockTime(0, 10).getOrDefault
        val updatedDtt1 = dynamicTimetable1.departureUpdate(Id(dynamicTimetable1.departureTime) + delay).getOrElse(dynamicTimetable1)
        val tai = TrainAgentInfo(trainAgent3905, Seq(dynamicTimetable2, updatedDtt1))
        tai.currentDynamicTimetable shouldBe Some(updatedDtt1)
        tai.delayInCurrentTimetable.map(_.toSeconds) shouldBe Some(delay.asTime).map(_.toSeconds)

