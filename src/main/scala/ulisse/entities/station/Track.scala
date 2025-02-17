package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validatePositive

trait Track:
  val platform: Int
  val train: Option[Train]

object Track:

  def apply(platformNumber: Int): Either[NonEmptyChain[Error], Track] =
    validatePositive(platformNumber, Error.InvalidPlatformNumber).toValidatedNec.toEither.map(TrackImpl(_, None))

  def generateSequentialTracks(numberOfTracks: Int): List[Track] =
    val step: Int => Int = _ + 1
    List.tabulate(numberOfTracks)(i => TrackImpl(step(i), None))

  enum Error extends BaseError:
    case InvalidPlatformNumber

  private final case class TrackImpl(platform: Int, train: Option[Train]) extends Track
