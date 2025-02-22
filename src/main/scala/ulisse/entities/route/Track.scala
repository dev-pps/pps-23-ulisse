package ulisse.entities.route

import cats.data.{NonEmptyChain, ValidatedNec}
import cats.syntax.all.*
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateRange, validateUniqueItems}

import scala.annotation.targetName
class TrackConfiguration(val minPermittedDistanceBetweenTrains: Double, val trackLength: Double)
trait Track:
  val trains: Seq[TrainAgent]
  def configuration: TrackConfiguration
  def isAvailable: Boolean = trains.forall(t => t.distanceTravelled - t.length >= configuration.minPermittedDistanceBetweenTrains)
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def filterNot(p: TrainAgent => Boolean): Track
  export trains.{contains, exists, isEmpty}

object Track:
  def apply(trains: TrainAgent*)(using configuration: TrackConfiguration): Track =
    TrackImpl(trains.distinctBy(_.name).filter(_.length <= configuration.trackLength).filter(t =>
      t.distanceTravelled + t.length <= configuration.trackLength
    ))
  def createCheckedTrack(
      trains: TrainAgent*
  )(using configuration: TrackConfiguration): Either[NonEmptyChain[Track.Errors], Track] =
    
      validateUniqueItems(trains.map(_.name), Errors.DuplicateTrains).toValidatedNec.map(_ => Track(trains*))
      .toEither

  private final case class TrackImpl(trains: Seq[TrainAgent])(using val configuration: TrackConfiguration)
      extends Track:
    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      val moved =
        if train.distanceTravelled > 0 then
          Left(Errors.TrainAlreadyMoved)
        else
          Right(())

      (
        moved.toValidatedNec,
        Track.createCheckedTrack(trains :+ train*).toValidated
      ).mapN((_, t) => t).toEither

    override def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent)
        : Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(trains.updateWhen(p)(f)*)
    override def filterNot(p: TrainAgent => Boolean): Track = copy(trains = trains.filterNot(p))

  enum Errors extends BaseError:
    case DuplicateTrains, TrainAlreadyMoved
