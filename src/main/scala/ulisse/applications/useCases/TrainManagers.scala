package ulisse.applications.useCases

import ulisse.entities.train.Technology
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.{UseType, Wagon}

import scala.util.Either

object TrainManagers:

  /** Train Manager errors that can be returned after a request.
    * @param description
    *   Errors description
    */
  enum Errors(val description: String):
    case TrainAlreadyExists(msg: String)
        extends Errors("Train already exist")
    case TechnologyAlreadyExists(msg: String)
        extends Errors("Technology already exist")
    case TrainNotExists(name: String) extends Errors(s"Train $name not exist")
    case TechnologyNotExists(name: String)
        extends Errors(s"Technology not exist")
    case WagonTypeUnknown(name: String)
        extends Errors(s"Wagon type $name not exist")

  trait TrainManager:
    /** Add train to train collection.
      *
      * @param train
      *   train to be added
      * @return
      *   Returns [[Right]] of `List[Train]` if train is added else [[Left]] of [[Errors.TrainAlreadyExists]]
      */
    def addTrain(train: Train): Either[Errors, List[Train]]

    /** @param name
      *   train name
      * @param technologyName
      *   train technology name
      * @param wagonTypeName
      *   name of wagon type
      * @param wagonCapacity
      *   wagon capacity
      * @param wagonCount
      *   amount of wagons
      * @return
      *   Returns [[Right]] of Train if train is added else [[Left]] of [[Errors.TrainAlreadyExists]]
      */
    def createTrain(
        name: String,
        technologyName: String,
        wagonTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]]

    /** Remove train from train collection.
      *
      * @param name
      *   train name to be removed
      * @return
      *   Returns [[Right]] of list of Train if train is removed else [[Left]] of [[Errors.TrainNotExists]]
      */
    def removeTrain(name: String): Either[Errors, List[Train]]

    /** Updates the information of the train that has the given name.
      *
      * @param name
      *   Train name
      * @param technology
      *   Train technology
      * @param wagon
      *   wagon type
      * @param wagonCount
      *   wagon amount
      * @return
      *   Returns [[Right]] of Train if train is updated else [[Left]] of [[Errors]]
      */
    def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, List[Train]]

    /** Add new train technology.
      *
      * @param technology
      *   technology to be added
      * @return
      *   [[Right]] of Technology if it is added else [[Left]] of [[Errors.TrainAlreadyExists]]
      */
    def addTechnology(technology: Technology): Either[Errors, List[Technology]]

    /** Remove technology.
      *
      * @param name
      *   technology name to be removed
      * @return
      *   Returns [[Right]] of List[Technology] if it is removed otherwise [[Left]] of [[Errors.TrainNotExists]]
      */
    def removeTechnology(name: String): Either[Errors, List[Technology]]

    /** @return
      *   Returns list of trains
      */
    def trains: List[Train]

    /** @return
      *   Returns list of train technologies
      */
    def technologies: List[Technology]

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
    /** @param initialState
      *   Initial technologies saved.
      * @return
      *   TrainService
      */
    def apply(initialState: List[Technology]): TrainManager = DefaultManager()

  private class DefaultManager extends TrainManager:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _trains: List[Train] = List.empty
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _technologies: Map[String, Technology] = Map.empty

    override def addTrain(train: Train): Either[Errors, List[Train]] =
      createTrain(
        train.name,
        train.techType.name,
        train.wagon.use.name,
        train.wagon.capacity,
        train.wagonCount
      )

    override def createTrain(
        name: String,
        technologyName: String,
        wagonTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]] =
      for
        _ <- trains.find(_.name.contentEquals(name)).map(t => Errors.TrainAlreadyExists(t.name)).toLeft(trains)
        tk <-
          technologies.find(_.name.contentEquals(technologyName)).toRight(Errors.TechnologyNotExists(technologyName))
        w <- wagonTypes.find(_.name.contentEquals(wagonTypeName)).toRight(Errors.WagonTypeUnknown(wagonTypeName))
      yield
        _trains = Train(name, tk, Wagon(w, wagonCapacity), wagonCount) :: _trains
        _trains

    override def removeTrain(name: String): Either[Errors, List[Train]] =
      trains.find(_.name.contentEquals(name))
        .map(_ =>
          _trains = trains.filterNot(_.name.contentEquals(name))
          trains
        ).toRight(Errors.TrainNotExists(name))

    override def addTechnology(technology: Technology): Either[Errors, List[Technology]] =
      import Errors.TechnologyAlreadyExists
      _technologies.get(technology.name)
        .map(_ => TechnologyAlreadyExists(technology.name))
        .toLeft({
          _technologies = _technologies.updated(technology.name, technology)
          technologies
        })

    override def removeTechnology(name: String): Either[Errors, List[Technology]] =
      import Errors.TechnologyNotExists
      _technologies.get(name).map(t =>
        _technologies = _technologies.removed(t.name)
        technologies
      ).toRight(TechnologyNotExists(name))

    override def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, List[Train]] =
      for
        _ <- removeTrain(name)
        ts <- addTrain(Train(
          name,
          technology,
          wagon,
          wagonCount
        ))
      yield ts

    override def trains: List[Train]            = _trains
    override def technologies: List[Technology] = _technologies.values.toList
    override def wagonTypes: List[UseType]      = UseType.values.toList
