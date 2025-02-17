package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validatePositive

/** Defines a track in a station. */
trait Track:
  val platform: Int
  val train: Option[Train]

/** Factory for [[Track]] instances. */
object Track:

  /** Creates a `Track` instance. If platform number is not positive it is set to 1 */
  def apply(platformNumber: Int): Track = TrackImpl(math.max(1, platformNumber), None)

  /** Creates a `Track` instance with validation. If platform number is not positive an error is returned */
  def createCheckedTrack(platformNumber: Int): Either[NonEmptyChain[Error], Track] =
    validatePositive(platformNumber, Error.InvalidPlatformNumber).toValidatedNec.toEither.map(Track(_))

  /** Creates a List of `Track` instance. If the specified numberOfTracks is not positive an empty List is returned */
  def generateSequentialTracks(numberOfTracks: Int): List[Track] =
    val step: Int => Int = _ + 1
    List.tabulate(numberOfTracks)(i => TrackImpl(step(i), None))

  extension (track: Track)
    def withTrain(train: Option[Train]): Track = TrackImpl(track.platform, train)

  /** Represents errors that can occur during `Track` creation. */
  enum Error extends BaseError:
    case InvalidPlatformNumber

  private final case class TrackImpl(platform: Int, train: Option[Train]) extends Track
