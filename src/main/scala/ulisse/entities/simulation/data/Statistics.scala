package ulisse.entities.simulation.data

import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.utils.Times.Time

/** Statistics for the simulation */
object Statistics:

  extension (tai: TrainAgentInfo)
    /** Get the current dynamic timetable for a train */
    def currentDynamicTimetable: Option[DynamicTimetable] = tai.timetables.sortBy(_.departureTime).find(!_.completed)

    /** Get the delay in the current dynamic timetable for a train */
    def delayInCurrentTimetable: Option[Time] = currentDynamicTimetable.flatMap(_.currentDelay)

  extension (environment: RailwayEnvironment)
    private def collectDelay(timeF: DynamicTimetable => Option[Time]): Time =
      Time.secondsToOverflowTime(
        environment
          .timetables
          .flatMap(timeF)
          .map(_.toSeconds).sum
      )

    /** Get the percentage of stations load */
    def percStationsLoad: Double =
      trainsInStations.toDouble / environment.stations.map(_.numberOfPlatforms).sum

    /** Get the number of trains on routes */
    def trainsOnRoutes: Int =
      environment.routes.collectTrains.size

    /** Get the number of trains in stations */
    def trainsInStations: Int =
      environment.stations.collectTrains.size

    /** Get the percentage of trains on routes */
    def percTrainsOnRoutes: Double =
      trainsOnRoutes.toDouble / environment.trains.size

    /** Get the percentage of trains in stations */
    def percTrainsInStations: Double =
      trainsInStations.toDouble / environment.trains.size

    /** Get the cumulative delay of all the timetables */
    def cumulativeDelay: Time =
      environment.collectDelay(_.currentDelay)

    /** Get the average delay of all the timetables */
    def averageDelay: Time =
      environment.timetables.count(_.currentDelay.isDefined) match
        case 0 => Time(0, 0, 0)
        case n => Time.secondsToOverflowTime(cumulativeDelay.toSeconds / n)

    /** Get the cumulative delay of a station */
    def cumulativeDelayIn(see: Station): Time =
      environment.collectDelay(_.delayIn(see))

    /** Get the average delay of a station */
    def averageDelayIn(see: Station): Time =
      environment.timetables.count(_.delayIn(see).isDefined) match
        case 0 => Time(0, 0, 0)
        case n => Time.secondsToOverflowTime(cumulativeDelayIn(see).toSeconds / n)
