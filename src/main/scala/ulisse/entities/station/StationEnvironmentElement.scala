package ulisse.entities.station

import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.simulation.Environments
import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.when

trait StationEnvironmentElement extends Station with EnvironmentElement:
  override type TrainContainer = Platform
  val platforms: List[TrainContainer]
  def firstAvailablePlatform: Option[TrainContainer] = platforms.find(_.train.isEmpty)
  def updatePlatform(platform: Platform, train: Option[TrainAgent]): Option[StationEnvironmentElement]
  def putTrain(trainContainer: TrainContainer, train: TrainAgent): Option[StationEnvironmentElement]
  def updateTrain(train: TrainAgent): Option[StationEnvironmentElement]
  def removeTrain(train: TrainAgent): Option[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Platform.generateSequentialPlatforms(station.numberOfTracks))

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailablePlatform.flatMap(track => station.updatePlatform(track, Some(train)))

    def leave(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.removeTrain(train)

    def findInStations(stations: Seq[StationEnvironmentElement]): Option[StationEnvironmentElement] =
      // TODO check impl
      stations.find(_.platforms.exists(_.train.map(_.name).contains(train.name)))

  private final case class StationEnvironmentElementImpl(station: Station, platforms: List[Platform])
      extends StationEnvironmentElement:
    export station.*

    def updatePlatform(platform: Platform, train: Option[TrainAgent]): Option[StationEnvironmentElement] =
      copy(platforms = platforms.updateWhen(_ == platform)(_.withTrain(train))) when !platforms.exists(
        train.isDefined && _.train == train
      )

    def putTrain(trainContainer: TrainContainer, train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_ == trainContainer).flatMap(updatePlatform(_, Some(train)))
    def updateTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_.train.contains(train)).flatMap(updatePlatform(_, Some(train)))
    def removeTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_.train.contains(train)).flatMap(updatePlatform(_, None))
