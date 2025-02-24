package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgent
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.validatePositive

trait Platform2 extends Platform

/** Defines a track in a station. */
trait Platform extends TrainAgentsContainer:
  // TODO evaluate if is needed a value for length of the platform track
  val container: Option[TrainAgent]
  override def trains: Seq[TrainAgent]                   = container.toList
  override def isAvailable: Boolean                      = container.isEmpty
  override def contains(train: TrainAgent): Boolean      = container.exists(train.matchId)
  override def minPermittedDistanceBetweenTrains: Double = if !isAvailable then 0.0 else Double.MaxValue

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

  private final case class PlatformImpl(id: Int, container: Option[TrainAgent]) extends Platform:
    // TODO evaluate if could be nice to introduce a control to check if the train is entering the station or is already moved
    override def putTrain(train: TrainAgent): Option[Platform] =
      copy(container = Some(train)) when isAvailable
    override def updateTrain(train: TrainAgent): Option[Platform] =
      copy(container = Some(train)) when contains(train)
    override def removeTrain(train: TrainAgent): Option[Platform] =
      copy(container = None) when contains(train)
