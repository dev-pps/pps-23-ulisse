package ulisse.entities.simulation.environments.railwayEnvironment

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhenWithEffects

trait ConfigurationData:
  def stations: Seq[StationEnvironmentElement]
  def routes: Seq[RouteEnvironmentElement]
  def timetables: Map[String, Seq[DynamicTimetable]]

object ConfigurationData:
  def apply(
      stations: Seq[StationEnvironmentElement],
      routes: Seq[RouteEnvironmentElement],
      trains: Seq[TrainAgent],
      timetables: Seq[DynamicTimetable]
  ): ConfigurationData =
    val sortedTimetablesByTrainId = orderedTimetablesByTrainId(trains.distinctBy(_.name), timetables.distinctBy(_.id))
    val stationsEEInitialState    = sortedTimetablesByTrainId.putTrainsInInitialStations(stations.distinctBy(_.name))
    val sortedTimetables = sortedTimetablesByTrainId.filter(t =>
      stationsEEInitialState.collectTrains.contains(t._1)
    ).map(t => (t._1.name, t._2)).toMap
    ConfigurationDataImpl(stationsEEInitialState, routes.distinctBy(_.id), sortedTimetables)

  private def orderedTimetablesByTrainId(
      trains: Seq[TrainAgent],
      timetables: Seq[DynamicTimetable]
  ): List[(TrainAgent, Seq[DynamicTimetable])] =
    def mapTrainsWithTimeTables(
        trains: Seq[TrainAgent],
        timetables: Seq[DynamicTimetable]
    ): Seq[(TrainAgent, DynamicTimetable)] =
      timetables.flatMap: tt =>
        trains.find(_.name == tt.train.name) match
          case Some(trainAgent) => Some(trainAgent -> tt)
          case _                => None

    def groupByTrainId(timetables: Seq[(TrainAgent, DynamicTimetable)]): List[(TrainAgent, Seq[DynamicTimetable])] =
      timetables.groupBy(_._1).view.mapValues(_.map(_._2)).toList

    def sortTimetablesByDepartureTime(timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
        : List[(TrainAgent, Seq[DynamicTimetable])] =
      timetablesByTrainId.map(e => (e._1, e._2.sortBy(_.departureTime)))

    sortTimetablesByDepartureTime(groupByTrainId(mapTrainsWithTimeTables(trains, timetables)).sortBy(_._1.name))

  extension (sortedTimetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
    private def putTrainsInInitialStations(stationsEE: Seq[StationEnvironmentElement]): Seq[StationEnvironmentElement] =

      def takeFirstTimetableForTrains(timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
          : List[(TrainAgent, Option[DynamicTimetable])] =
        timetablesByTrainId.map(e => (e._1, e._2.headOption))

      def updateStationEE(
          stationsEE: Seq[StationEnvironmentElement],
          timetable: DynamicTimetable,
          trainAgent: TrainAgent
      ): Option[Seq[StationEnvironmentElement]] =
        stationsEE.updateWhenWithEffects(station => station.name == timetable.startStation.name)(
          _.putTrain(trainAgent)
        )

      takeFirstTimetableForTrains(sortedTimetablesByTrainId).foldLeft(stationsEE)((stationsEE, tt) =>
        tt._2.flatMap(updateStationEE(stationsEE, _, tt._1)).getOrElse(stationsEE)
      )

  private final case class ConfigurationDataImpl(
      stations: Seq[StationEnvironmentElement],
      routes: Seq[RouteEnvironmentElement],
      timetables: Map[String, Seq[DynamicTimetable]]
  ) extends ConfigurationData
