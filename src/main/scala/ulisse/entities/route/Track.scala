package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.validateUniqueItems

import scala.annotation.targetName

trait Track:
  val trains: Seq[TrainAgent]
  def minPermittedDistanceBetweenTrains: Double
  def isAvailable: Boolean = trains.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def filterNot(p: TrainAgent => Boolean): Track
  export trains.{contains, exists, isEmpty}

object Track:
  def apply(minPermittedDistanceBetweenTrains: Double, trains: TrainAgent*): Track =
    TrackImpl(trains.distinctBy(_.name), minPermittedDistanceBetweenTrains)
  def createCheckedTrack(
      minPermittedDistanceBetweenTrains: Double,
      trains: TrainAgent*
  ): Either[NonEmptyChain[Track.Errors], Track] =
    validateUniqueItems(trains.map(_.name), Errors.DuplicateTrains).toValidatedNec
      .map(_ => Track(minPermittedDistanceBetweenTrains, trains*))
      .toEither
  private final case class TrackImpl(trains: Seq[TrainAgent], minPermittedDistanceBetweenTrains: Double) extends Track:
    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(minPermittedDistanceBetweenTrains, trains :+ train: _*)
    override def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent)
        : Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(minPermittedDistanceBetweenTrains, trains.updateWhen(p)(f): _*)
    override def filterNot(p: TrainAgent => Boolean): Track = copy(trains = trains.filterNot(p))

  enum Errors extends BaseError:
    case DuplicateTrains
