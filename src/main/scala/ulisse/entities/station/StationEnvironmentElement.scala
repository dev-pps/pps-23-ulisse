package ulisse.entities.station

import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(
      station,
      TrainAgentsContainer.generateSequentialContainers(Platform.apply, station.numberOfTracks)
    )

  extension (train: TrainAgent)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailableContainer.flatMap(platform => station.putTrain(platform, train))

  private final case class StationEnvironmentElementImpl(station: Station, containers: Seq[TrainAgentsContainer])
      extends StationEnvironmentElement:
    export station.*

    def putTrain(trainContainer: TrainAgentsContainer, train: TrainAgent): Option[StationEnvironmentElement] =
      containers.find(_ == trainContainer)
        .flatMap(_.putTrain(train))
        .map(updatedPlatform =>
          copy(containers = containers.updateWhen(_ == trainContainer)(_ => updatedPlatform))
        ) when !contains(train)

    override protected def buildNewEnvironmentElement(containers: Seq[TrainAgentsContainer])
        : StationEnvironmentElement = copy(containers = containers)
