package ulisse.entities.station

import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper:
  override def trains: Seq[TrainAgent]                     = containers.flatMap(_.trains)
  def firstAvailablePlatform: Option[TrainAgentsContainer] = containers.find(_.isAvailable)
  override def contains(train: TrainAgent): Boolean        = containers.exists(_.contains(train))

  override def putTrain(container: TrainAgentsContainer, train: TrainAgent): Option[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Platform.generateSequentialPlatforms(station.numberOfTracks))

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailablePlatform.flatMap(platform => station.putTrain(platform, train))

  private final case class StationEnvironmentElementImpl(station: Station, containers: List[TrainAgentsContainer])
      extends StationEnvironmentElement:
    export station.*

    override def containersIDs: Seq[Int] = containers.map(_.id)
    override def putTrain(trainContainer: TrainAgentsContainer, train: TrainAgent): Option[StationEnvironmentElement] =
      containers.find(_ == trainContainer)
        .flatMap(_.putTrain(train))
        .map(updatedPlatform =>
          copy(containers = containers.updateWhen(_ == trainContainer)(_ => updatedPlatform))
        ) when !contains(train)
    override def updateTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      containers.updateWhenWithEffects(_.contains(train))(_.updateTrain(train)).map(pfs =>
        copy(containers = pfs)
      ) when contains(train)
    override def removeTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      containers.updateWhenWithEffects(_.contains(train))(_.removeTrain(train)).map(pfs =>
        copy(containers = pfs)
      ) when contains(train)
