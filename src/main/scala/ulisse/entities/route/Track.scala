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
  def apply(trains: TrainAgent*)(using minPermittedDistanceBetweenTrains: Double, trackLength: Double): Track =
    TrackImpl(trains.distinctBy(_.name))
  def createCheckedTrack(
      trains: TrainAgent*
  )(using minPermittedDistanceBetweenTrains: Double, trackLength: Double): Either[NonEmptyChain[Track.Errors], Track] =
    (validateUniqueItems(trains.map(_.name), Errors.DuplicateTrains).toValidatedNec,
      validate
      .map(_ => Track(trains*))
      .toEither
  private final case class TrackImpl(trains: Seq[TrainAgent])(using val minPermittedDistanceBetweenTrains: Double) extends Track:
    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(trains :+ train*)
    override def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent)
        : Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(trains.updateWhen(p)(f)*)
    override def filterNot(p: TrainAgent => Boolean): Track = copy(trains = trains.filterNot(p))

  enum Errors extends BaseError:
    case DuplicateTrains
