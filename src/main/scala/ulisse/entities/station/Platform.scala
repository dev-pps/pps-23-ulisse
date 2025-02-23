package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validatePositive

import scala.annotation.targetName

/** Defines a track in a station. */
trait Platform:
  // TODO evaluate if is needed a value for length of the platform track
  val platformNumber: Int
  val train: Option[TrainAgent]
  def isAvailable: Boolean = train.isEmpty
  def withTrain(train: Option[TrainAgent]): Platform

/** Factory for [[Platform]] instances. */
object Platform:

  /** Creates a `Track` instance. If platform number is not positive it is set to 1 */
  def apply(platformNumber: Int): Platform = PlatformImpl(math.max(1, platformNumber), None)

  /** Creates a `Track` instance with validation. If platform number is not positive an error is returned */
  def createCheckedPlatform(platformNumber: Int): Either[NonEmptyChain[Errors], Platform] =
    validatePositive(platformNumber, Errors.InvalidPlatformNumber).toValidatedNec.toEither.map(Platform(_))

  /** Creates a List of `Track` instance. If the specified numberOfTracks is not positive an empty List is returned */
  def generateSequentialPlatforms(numberOfTracks: Int): List[Platform] =
    val step: Int => Int = _ + 1
    List.tabulate(numberOfTracks)(i => PlatformImpl(step(i), None))

  /** Represents errors that can occur during `Track` creation. */
  enum Errors extends BaseError:
    case InvalidPlatformNumber

  private final case class PlatformImpl(platformNumber: Int, train: Option[TrainAgent]) extends Platform:
    def withTrain(train: Option[TrainAgent]): Platform = copy(train = train)
