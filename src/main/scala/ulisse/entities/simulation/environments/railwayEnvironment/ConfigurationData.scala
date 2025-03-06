package ulisse.entities.simulation.environments.railwayEnvironment

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.updateWhenWithEffects
import ulisse.utils.Times.Time

/** Configuration data for the simulation */
trait ConfigurationData:
  /** Configured stations for the simulation */
  def stations: Seq[StationEnvironmentElement]

  /** Configured routes for the simulation, they are distinct and sorted by the best technology */
  def routes: Seq[RouteEnvironmentElement]

  /** Configured timetables for the simulation, they are divided by train and sorted by departure time. Note: trains that haven't a timetable are removed, trains with conflict on initial state are */
  def timetablesByTrain: Map[Train, Seq[DynamicTimetable]]

/** Factory for [[ConfigurationData]] instances */
object ConfigurationData:

  /** Create a new ConfigurationData considering:
    * - distinct station and with train placed respective to initial state derived from timetables
    * - distinct routes sorted by the best technology
    * - timetables divided by train and sorted by departure time
    * NOTE: trains that haven't a timetable are removed, trains with conflict on initial state are removed with all their timetables
    */
  def apply(
      stations: Seq[StationEnvironmentElement],
      routes: Seq[RouteEnvironmentElement],
      trains: Seq[TrainAgent],
      timetables: Seq[DynamicTimetable]
  ): ConfigurationData =
    val sortedTimetablesByTrainId = orderedTimetablesByTrainId(trains.distinctBy(_.name), timetables.distinctBy(_.id))
    val stationsEEInitialState    = sortedTimetablesByTrainId.putTrainsInInitialStations(stations.distinctBy(_.name))
    val sortedTimetables: Map[Train, Seq[DynamicTimetable]] = sortedTimetablesByTrainId.filter(t =>
      stationsEEInitialState.collectTrains.contains(t._1)
    ).toMap
    ConfigurationDataImpl(
      stationsEEInitialState,
      routes.distinctBy(_.id).sortBy(_.typology.technology).reverse,
      sortedTimetables
    )

  /** Create an empty ConfigurationData */
  def empty(): ConfigurationData =
    ConfigurationDataImpl(Seq.empty, Seq.empty, Map.empty)

  extension (configurationData: ConfigurationData)
    /** Get all the trains in the configuration */
    def trains: List[TrainAgent] =
      (configurationData.stations.collectTrains ++ configurationData.routes.collectTrains).toList

    /** Get all the timetables in the configuration */
    def timetables: Seq[DynamicTimetable] = configurationData.timetablesByTrain.values.flatten.toSeq

    /** Get the min departure time of the timetables or if no timetable is present returns Time(0,0,0) */
    def departureTime: Time =
      if configurationData.timetables.isEmpty then Time(0, 0, 0)
      else
        Time.secondsToTime(configurationData.timetables.map(_.departureTime.toSeconds).foldLeft(Int.MaxValue)(math.min))

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
      timetablesByTrain: Map[Train, Seq[DynamicTimetable]]
  ) extends ConfigurationData
