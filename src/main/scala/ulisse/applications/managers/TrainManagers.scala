package ulisse.applications.managers

import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist, ErrorValidation}

import scala.util.Either

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
    /** Add train to train collection.
      *
      * @param train
      *   train to be added
      * @return
      *   Returns [[Right]] of updated `TrainManager` if train is added else [[Left]] of
      *   [[TrainErrors.TrainAlreadyExists]]
      */
    def addTrain(train: Train): Either[TrainErrors, TrainManager]

    /** @param name
      *   train name
      * @param technology
      *   train technology
      * @param wagonTypeName
      *   name of wagon type
      * @param wagonCapacity
      *   wagon capacity
      * @param length
      *   length of train (amount of wagons)
      * @return
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

    /** Remove train from train collection.
      *
      * @param name
      *   train name to be removed
      * @return
      *   Returns [[Right]] of updated `TrainManager` if train is removed else [[Left]] of
      *   [[TrainErrors.TrainNotExists]]
      */
    def removeTrain(name: String): Either[TrainErrors, TrainManager]

    /** Updates the information of the train that has the given name.
      *
      * @param name
      *   Train name
      * @param technology
      *   Train technology
      * @param wagonUseName
      *   wagon use name
      * @param wagonCapacity
      *   wagon capacity
      * @param length
      *   train length (wagon amount)
      * @return
      *   Returns [[Right]] of updated `TrainManager` if train is updated else [[Left]] of [[TrainErrors]]
      */
    def updateTrain(name: String)(
        technology: TrainTechnology,
        wagonUseName: String,
        wagonCapacity: Int,
        length: Int
    ): Either[TrainErrors, TrainManager]

    /** @return
      *   Returns list of trains
      */
    def trains: List[Train]

    /** @return
      *   Returns List of wagons [[UseType]]
      */
    def wagonTypes: List[UseType]

  /** Companion object of the trait `TrainManager`.
    *
    * @see
    *   [[TrainManager]] for more detailed behaviour definition.
    */
  object TrainManager:
    /** @param trains
      *   Trains saved
      * @return
      *   TrainManager
      */
    def apply(trains: List[Train]): TrainManager = DefaultManager(trains)

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
          _ <- findTrain(name).map(t => TrainErrors.TrainAlreadyExists(t.name)).toLeft(trains)
          w <- wagonTypes.find(_.name.contentEquals(wagonTypeName)).toRight(TrainErrors.WagonTypeUnknown(wagonTypeName))
          wc <- wagonCapacity.validatePositiveValue("wagon capacity")
          c  <- length.validatePositiveValue("train length")
        yield TrainManager(Train(name, technology, Wagon(w, wc), c) :: trains)

      extension (value: Int)
        private def validatePositiveValue(name: String): Either[TrainErrors.NegativeValue, Int] =
          if value > 0 then Right(value) else Left(TrainErrors.NegativeValue(name, value))

      override def removeTrain(name: String): Either[TrainErrors, TrainManager] =
        findTrain(name)
          .map(_ =>
            TrainManager(trains.filterNot(_.name.contentEquals(name)))
          ).toRight(TrainErrors.TrainNotExists(name))

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

      private def findTrain(name: String): Option[Train] = trains.find(_.name.contentEquals(name))
