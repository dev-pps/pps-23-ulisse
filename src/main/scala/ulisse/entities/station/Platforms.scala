package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.station.Platforms.Errors
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.validatePositive

object Platforms:

  /** Errors for Platform */
  enum Errors extends BaseError:
    case InvalidPlatformId

  /** Defines a rail in a station. */
  trait Platform extends TrainAgentsContainer[Platform]:
    /** Try to put train inside the platform */
    def putTrain(train: TrainAgent): Option[Platform]

    /** Check if the platform is available for a train to be put in */
    def isAvailable: Boolean = isEmpty

  /** Factory for [[Platform]] instances. */
  object Platform:

    /** Creates a `Platform` instance. If platform id is not positive it is set to 1 */
    def apply(id: Int): Platform = PlatformImpl(math.max(1, id), None)

    /** Creates a `Platform` instance with validation. If platform id is not positive an error is returned */
    def createCheckedPlatform(id: Int): Either[NonEmptyChain[Errors], Platform] =
      validatePositive(id, Errors.InvalidPlatformId).toValidatedNec.toEither.map(Platform(_))

    private final case class PlatformImpl(
        id: Int,
        currentTrain: Option[TrainAgent]
    ) extends Platform:
      override def trains: Seq[TrainAgent] = currentTrain.toList

      override def putTrain(train: TrainAgent): Option[Platform] =
        copy(currentTrain = Some(train)) when isAvailable && train.motionData.distanceTravelled == 0

      override def updateTrain(train: TrainAgent): Option[Platform] =
        copy(currentTrain = Some(train)) when contains(train)

      override def removeTrain(train: TrainAgent): Option[Platform] =
        copy(currentTrain = None) when contains(train)
