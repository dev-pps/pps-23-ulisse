package ulisse.entities.simulation.data

import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.utils.Times.Time

object Statistics:

  extension (tai: TrainAgentInfo)
    def currentDynamicTimetable: Option[DynamicTimetable] = tai.timetables.sortBy(_.departureTime).find(!_.completed)
    def delayInCurrentTimetable: Option[Time]             = currentDynamicTimetable.flatMap(_.currentDelay)

  extension (environment: RailwayEnvironment)
    def cumulativeDelay: Time =
      Time.secondsToOverflowTime(
        environment
          .timetables
          .flatMap(_.currentDelay)
          .map(_.toSeconds).sum
      )

    def averageDelay: Time =
      Time.secondsToOverflowTime(
        cumulativeDelay.toSeconds
          /
            environment.timetables.count(_.currentDelay.isDefined)
      )

    def cumulativeDelayIn(see: StationEnvironmentElement): Time =
      Time.secondsToOverflowTime(
        environment
          .timetables
          .flatMap(_.delayIn(see))
          .map(_.toSeconds).sum
      )

    def averageDelayIn(see: StationEnvironmentElement): Time =
      Time.secondsToOverflowTime(
        cumulativeDelayIn(see).toSeconds
          /
            environment.timetables.count(_.delayIn(see).isDefined)
      )
