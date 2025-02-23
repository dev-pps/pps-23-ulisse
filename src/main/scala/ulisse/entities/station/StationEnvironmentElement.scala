package ulisse.entities.station

import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.simulation.Environments
import ulisse.entities.simulation.Environments.{EnvironmentElement, StationEnvironmentElement2, TrainAgentEEWrapper}
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.OptionUtils.given_Conversion_Option_Option

import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.when

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
      stations.find(_.platforms.exists(_.train.map(_.name).contains(train.name)))

  private final case class StationEnvironmentElementImpl(station: Station, platforms: List[Platform])
      extends StationEnvironmentElement:
    export station.*
    extension (train: TrainAgent)
      private def existIn(platforms: List[Platform]): Boolean =
        platforms.exists(_.train.map(_.name).contains(train.name))

    def putTrain(trainContainer: TAC, train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.find(_ == trainContainer)
        .flatMap(_.putTrain(train))
        .map(updatedPlatform =>
          copy(platforms = platforms.updateWhen(_ == trainContainer)(_ => updatedPlatform))
        ) when !(train existIn platforms)
    def updateTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(_.train.map(_.name).contains(train.name))(_.updateTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when (train existIn platforms)
    def removeTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      platforms.updateWhenWithEffects(_.train.map(_.name).contains(train.name))(_.removeTrain(train)).map(pfs =>
        copy(platforms = pfs)
      ) when (train existIn platforms)
