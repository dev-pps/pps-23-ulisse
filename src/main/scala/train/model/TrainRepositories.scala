package train.model

import train.model.TrainRepositories.RepoError.AlreadyExisting
import train.model.Trains.Train

object TrainRepositories:

  enum RepoError(val description: String):
    case AlreadyExisting(msg: String)
        extends RepoError("Train already exist")

  trait TrainRepository:
    /** Add train to train collection.
      * @param train
      *   train to be added
      * @return
      *   Returns [[Right]] of Train if train is added else [[Left]] of
      *   [[RepoError.AlreadyExisting]]
      */
    def add(train: Train): Either[RepoError, Train]

    /** @return
      *   Returns list of trains
      */
    def trains: List[Train]

  object TrainRepository:
    private var _trains: List[Train] = List.empty

    def add(train: Train): Either[RepoError, Train] =
      _trains.find(_.name.contentEquals(train.name)) match
        case Some(t) => Left(AlreadyExisting(t.name))
        case None =>
          _trains = train :: _trains
          Right(train)

    def trains: List[Train] = _trains
