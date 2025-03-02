package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.validatePositive

/** Defines a track in a station. */
trait Platform extends TrainAgentsContainer[Platform]:
  def putTrain(train: TrainAgent): Option[Platform]
  def isAvailable: Boolean = isEmpty

/** Factory for [[Platform]] instances. */
object Platform:

  /** Creates a `Track` instance. If platform number is not positive it is set to 1 */
  def apply(platformNumber: Int): Platform = PlatformImpl(math.max(1, platformNumber), None)

  /** Creates a `Track` instance with validation. If platform number is not positive an error is returned */
  def createCheckedPlatform(platformNumber: Int): Either[NonEmptyChain[Errors], Platform] =
    validatePositive(platformNumber, Errors.InvalidPlatformNumber).toValidatedNec.toEither.map(Platform(_))

  /** Represents errors that can occur during `Track` creation. */
  enum Errors extends BaseError:
    case InvalidPlatformNumber

  private final case class PlatformImpl(
      id: Int,
      currentTrain: Option[TrainAgent]
  ) extends Platform:
    override def trains: Seq[TrainAgent] = currentTrain.toList

    override def putTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = Some(train)) when isAvailable && train.distanceTravelled == 0

    // TODO make sense to not allow train moving? since we consider station as a point
    override def updateTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = Some(train)) when contains(train)

    override def removeTrain(train: TrainAgent): Option[Platform] =
      copy(currentTrain = None) when contains(train)
