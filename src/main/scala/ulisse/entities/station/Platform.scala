package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.validatePositive

/** Defines a rail in a station. */
trait Platform extends TrainAgentsContainer[Platform]:
  /** Try to put train inside the platform */
  def putTrain(train: TrainAgent): Option[Platform]

  /** Check if the platform is available for a train to be put in */
  def isAvailable: Boolean = isEmpty

/** Factory for [[Platform]] instances. */
object Platform:
  /** Minimum platform id. */
  val minPlatformId: Int = 1

  /** Creates a `Platform` instance. If platform id is lower than minPlatformId it's set to that value */
  def apply(id: Int): Platform = PlatformImpl(math.max(minPlatformId, id), None)

  /** Creates a `Platform` instance with validation. If platform id is not positive an error is returned */
  def createCheckedPlatform(id: Int): Either[NonEmptyChain[Error], Platform] =
    validatePositive(id, Error.InvalidPlatformId).toValidatedNec.toEither.map(Platform(_))

  /** Represents errors that can occur during `Platforms` creation. */
  enum Error extends BaseError:
    case InvalidPlatformId

  private final case class PlatformImpl(
      id: Int,
      currentTrain: Option[TrainAgent]
  ) extends Platform:
    override def trains: Seq[TrainAgent] = currentTrain.toList

    override def putTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = Some(train)) when isAvailable && train.distanceTravelled == 0

    override def updateTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = Some(train)) when contains(train)

    override def removeTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = None) when contains(train)
