package ulisse.entities.station

import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.simulation.Environments.{EnvironmentElement, EnvironmentElementContainer}
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.when

trait StationEnvironmentElement extends Station with EnvironmentElement[StationEnvironmentElement]:
  val platforms: List[Platform]
  def firstAvailablePlatform: Option[Platform] = platforms.find(_.train.isEmpty)
  def updatePlatform(platform: Platform, train: Option[TrainAgent]): Option[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Platform.generateSequentialPlatforms(station.numberOfTracks))

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailablePlatform.flatMap(track => station.updatePlatform(track, Some(train)))

    def leave(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.platforms.find(_.train.contains(train)).flatMap(track => station.updatePlatform(track, None))

    def findInStation(stations: Seq[StationEnvironmentElement]): Option[StationEnvironmentElement] =
      // TODO check impl
      stations.find(_.platforms.exists(_.train.map(_.name).contains(train.name)))

  private final case class StationEnvironmentElementImpl(station: Station, platforms: List[Platform])
      extends StationEnvironmentElement:
    export station.*

    override def putAgent(
        container: EnvironmentElementContainer[?],
        agent: SimulationAgent
    ): Option[StationEnvironmentElement] =
      (container, agent) match
        case (platform: Platform, train: TrainAgent) => updatePlatform(platform, Some(train))
        case _                                       => None

    override def removeAgent(agent: SimulationAgent): Option[StationEnvironmentElement] =
      agent match
        case train: TrainAgent => platforms.find(_.train.contains(train)).flatMap(track => updatePlatform(track, None))
        case _                 => None

    override def updateAgent(agent: SimulationAgent): Option[StationEnvironmentElement] =
      agent match
        case train: TrainAgent =>
          platforms.find(_.train.contains(train)).flatMap(track => updatePlatform(track, Some(train)))
        case _ => None

    def updatePlatform(platform: Platform, train: Option[TrainAgent]): Option[StationEnvironmentElement] =
      copy(platforms = platforms.updateWhen(_ == platform)(_.withTrain(train))) when !platforms.exists(
        train.isDefined && _.train == train
      )
