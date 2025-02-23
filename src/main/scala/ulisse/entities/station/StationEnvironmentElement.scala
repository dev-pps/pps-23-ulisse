package ulisse.entities.station

import ulisse.entities.simulation.Environments
import ulisse.entities.simulation.Environments.TrainAgentEEWrapper
import ulisse.entities.station.Platform.existInPlatform
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]:
  override type TAC = Platform
  val platforms: List[TAC]
  def firstAvailablePlatform: Option[TAC] = platforms.find(_.train.isEmpty)

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Platform.generateSequentialPlatforms(station.numberOfTracks))

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailablePlatform.flatMap(platform => station.putTrain(platform, train))

    def leave(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.removeTrain(train)

    def findInStations(stations: Seq[StationEnvironmentElement]): Option[StationEnvironmentElement] =
      stations.find(train.existInStation)

    def existInStation(station: StationEnvironmentElement): Boolean =
      station.platforms.exists(train.existInPlatform)

  private final case class StationEnvironmentElementImpl(station: Station, platforms: List[Platform])
      extends StationEnvironmentElement:
    export station.*

    def putTrain(trainContainer: TAC, train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_ == trainContainer)
        .flatMap(_.putTrain(train))
        .map(updatedPlatform =>
          copy(platforms = platforms.updateWhen(_ == trainContainer)(_ => updatedPlatform))
        ) when !(train existInStation this)
    def updateTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(train.existInPlatform)(_.updateTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when (train existInStation this)
    def removeTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(train.existInPlatform)(_.removeTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when (train existInStation this)
