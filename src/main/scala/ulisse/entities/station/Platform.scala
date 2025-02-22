package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validatePositive

/** Defines a track in a station. */
trait Platform:
  val platformNumber: Int
  val train: Option[Train]

/** Factory for [[Platform]] instances. */
object Platform:

  /** Creates a `Track` instance. If platform number is not positive it is set to 1 */
  def apply(platformNumber: Int): Platform = TrackImpl(math.max(1, platformNumber), None)

  /** Creates a `Track` instance with validation. If platform number is not positive an error is returned */
  def createCheckedPlatform(platformNumber: Int): Either[NonEmptyChain[Error], Platform] =
    validatePositive(platformNumber, Error.InvalidPlatformNumber).toValidatedNec.toEither.map(Platform(_))

  /** Creates a List of `Track` instance. If the specified numberOfTracks is not positive an empty List is returned */
  def generateSequentialPlatforms(numberOfTracks: Int): List[Platform] =
    val step: Int => Int = _ + 1
    List.tabulate(numberOfTracks)(i => TrackImpl(step(i), None))

  extension (track: Platform)
    def withTrain(train: Option[Train]): Platform = TrackImpl(track.platformNumber, train)

  /** Represents errors that can occur during `Track` creation. */
  enum Error extends BaseError:
    case InvalidPlatformNumber

  private final case class TrackImpl(platformNumber: Int, train: Option[Train]) extends Platform
