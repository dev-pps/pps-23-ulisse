package ulisse.entities.station

import ulisse.entities.simulation.EnvironmentElements.TrainAgentEEWrapper
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]:
  override type TAC = Platform
  val platforms: List[TAC]
  def firstAvailablePlatform: Option[TAC]           = platforms.find(_.train.isEmpty)
  override def contains(train: TrainAgent): Boolean = platforms.exists(_.contains(train))

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Platform.generateSequentialPlatforms(station.numberOfTracks))

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailablePlatform.flatMap(platform => station.putTrain(platform, train))

  private final case class StationEnvironmentElementImpl(station: Station, platforms: List[Platform])
      extends StationEnvironmentElement:
    export station.*

    def putTrain(trainContainer: TAC, train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_ == trainContainer)
        .flatMap(_.putTrain(train))
        .map(updatedPlatform =>
          copy(platforms = platforms.updateWhen(_ == trainContainer)(_ => updatedPlatform))
        ) when !contains(train)
    def updateTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(_.contains(train))(_.updateTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when contains(train)
    def removeTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(_.contains(train))(_.removeTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when contains(train)
