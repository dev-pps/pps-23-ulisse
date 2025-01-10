package train.model

import train.model.TrainRepositories.Errors.{TrainAlreadyExists, TrainNotExists}
import train.model.Trains.Carriages.Carriage
import train.model.Trains.{TechnologyType, Train}

object TrainRepositories:

  /** @param description
    *   Error description
    */
  enum Errors(val description: String):
    case TrainAlreadyExists(msg: String)
        extends Errors("Train already exist")
    case TrainNotExists(msg: String) extends Errors("Train not exist")
    case Unclassified(msg: String)   extends Errors(s"Unclassified error: $msg")

  trait TrainRepository:
    /** Add train to train collection.
      *
      * @param train
      *   train to be added
      * @return
      *   Returns [[Right]] of Train if train is added else [[Left]] of
      *   [[Errors.TrainAlreadyExists]]
      */
    def add(train: Train): Either[Errors, Train]

    /** Remove train from train collection.
      *
      * @param name
      *   train name to be removed
      * @return
      *   Returns [[Right]] of list of Train if train is removed else [[Left]]
      *   of [[Errors.TrainNotExists]]
      */
    def remove(name: String): Either[Errors, List[Train]]

    /** Updates the information of the train that has the given name.
      *
      * @param name
      *   Train name
      * @param technology
      *   Train technology
      * @param carriage
      *   Carriage type
      * @param carriageCount
      *   Carriage amount
      * @return
      *   Returns [[Right]] of Train if train is updated else [[Left]] of *
      *   [[Errors.TrainNotExists]]
      */
    def update(name: String)(
        technology: TechnologyType,
        carriage: Carriage,
        carriageCount: Int
    ): Either[Errors, Train]

    /** @return
      *   Returns list of trains
      */
    def trains: List[Train]

  object TrainRepository:
    private var _trains: List[Train] = List.empty

    private def findTrain(name: String): Option[Train] =
      _trains.find(_.name.contentEquals(name))

    def add(train: Train): Either[Errors, Train] =
      findTrain(train.name) match
        case Some(t) => Left(TrainAlreadyExists(t.name))
        case None =>
          _trains = train :: _trains
          Right(train)

    def remove(name: String): Either[Errors, List[Train]] =
      findTrain(name) match
        case Some(t) =>
          _trains = _trains.filterNot(_.name.contentEquals(name))
          Right(_trains)
        case None => Left(TrainNotExists(name))

    def update(name: String)(
        technology: TechnologyType,
        carriage: Carriage,
        carriageCount: Int
    ): Either[Errors, Train] =
      findTrain(name) match
        case Some(Train(n, _, _, _)) =>
          val train = Train(
            n,
            technology,
            carriage,
            carriageCount
          )
          remove(n) match
            case Left(e)  => Left(TrainNotExists(s"$e during update call"))
            case Right(_) => add(train)
        case None => Left(Errors.TrainNotExists(name))
        case _ =>
          Left(Errors.Unclassified("train not recognized in match case"))

    def trains: List[Train] = _trains
