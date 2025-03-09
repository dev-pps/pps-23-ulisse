package ulisse.applications.managers

import ulisse.applications.managers.TrainManagers.TrainErrors.{TrainAlreadyExists, TrainNotExists}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist, ErrorValidation}
import ulisse.utils.ValidationUtils

import scala.util.Either

/** Object containing definition of timetable manager and its errors. */
object TrainManagers:

  /** Train Manager errors that can be returned after a request. */
  sealed trait TrainErrors extends BaseError
  object TrainErrors:
    final case class TrainAlreadyExists(name: String) extends ErrorMessage(s"[DUPLICATE] train $name") with TrainErrors
    final case class TrainNotExists(name: String)     extends ErrorNotExist(s"train $name") with TrainErrors
    final case class WagonTypeUnknown(name: String)   extends ErrorNotExist(s"wagon type $name") with TrainErrors
    final case class NegativeValue(name: String, value: Int) extends ErrorValidation(s"negative $name value: $value")
        with TrainErrors

  trait TrainManager:
    /** Adds `train` to train collection.
      *
      * Returns [[Right]] of updated `TrainManager` if train is added else [[Left]] of [[TrainErrors.TrainAlreadyExists]]
      */
    def addTrain(train: Train): Either[TrainErrors, TrainManager]

    /** Returns Train given its `name`. */
    def findTrain(name: String): Either[TrainErrors, Train] =
      trains.find(_.name == name).toRight(TrainNotExists(name))

    /** Creates new [[Train]] given its `name`, `technology`, `wagonTypeName`, `wagonCapacity` and `length` (amount of wagons)
      *
      *   Returns [[Right]] of updated `TrainManager` if train is added else [[Left]] of
      *   [[TrainErrors.TrainAlreadyExists]]
      */
    def createTrain(
        name: String,
        technology: TrainTechnology,
        wagonTypeName: String,
        wagonCapacity: Int,
        length: Int
    ): Either[TrainErrors, TrainManager]

    /** Removes train from trains collection given its `name`.
      *
      * Returns [[Right]] of updated `TrainManager` if train is removed else [[Left]] of
      *   [[TrainErrors.TrainNotExists]]
      */
    def removeTrain(name: String): Either[TrainErrors, TrainManager]

    /** Updates the information of the train identified by `name`.
      *
      * Returns [[Right]] of updated `TrainManager` if train is updated else [[Left]] of [[TrainErrors]]
      */
    def updateTrain(name: String)(
        technology: TrainTechnology,
        wagonUseName: String,
        wagonCapacity: Int,
        length: Int
    ): Either[TrainErrors, TrainManager]

    /** Returns list of trains */
    def trains: List[Train]

    /** Returns List of wagons [[UseType]] */
    def wagonTypes: List[UseType]

  /** Companion object of the trait `TrainManager`.
    * @see [[TrainManager]] for more detailed behaviour definition.
    */
  object TrainManager:
    /** Returns TrainManager initialized with `trains` */
    def apply(trains: List[Train]): TrainManager = DefaultManager(trains)

    /** Create [[TrainManager]] with empty list of trains. */
    def empty(): TrainManager = DefaultManager(List.empty)

    def unapply(manager: TrainManager): Option[List[Train]] = Some(manager.trains)

    private case class DefaultManager(trains: List[Train]) extends TrainManager:

      override def addTrain(train: Train): Either[TrainErrors, TrainManager] =
        createTrain(
          train.name,
          train.techType,
          train.wagon.use.name,
          train.wagon.capacity,
          train.length
        )

      override def createTrain(
          name: String,
          technology: TrainTechnology,
          wagonTypeName: String,
          wagonCapacity: Int,
          length: Int
      ): Either[TrainErrors, TrainManager] =
        for
          _ <- findTrain(name).map(_ => TrainAlreadyExists(name)).toOption.toLeft(name)
          w <- wagonTypes.find(_.name.contentEquals(wagonTypeName)).toRight(TrainErrors.WagonTypeUnknown(wagonTypeName))
          wc <- wagonCapacity.validatePositiveValue("wagon capacity")
          c  <- length.validatePositiveValue("train length")
        yield TrainManager(Train(name, technology, Wagon(w, wc), c) :: trains)

      extension (value: Int)
        private def validatePositiveValue(name: String): Either[TrainErrors.NegativeValue, Int] =
          ValidationUtils.validatePositive(value, TrainErrors.NegativeValue(name, value))

      override def removeTrain(name: String): Either[TrainErrors, TrainManager] =
        findTrain(name)
          .map(_ =>
            TrainManager(trains.filterNot(_.name.contentEquals(name)))
          )

      override def updateTrain(name: String)(
          technology: TrainTechnology,
          wagonUseName: String,
          wagonCapacity: Int,
          length: Int
      ): Either[TrainErrors, TrainManager] =
        for
          r <- removeTrain(name)
          ts <- r.createTrain(
            name,
            technology,
            wagonUseName,
            wagonCapacity,
            length
          )
        yield ts

      override def wagonTypes: List[UseType] = UseType.values.toList
