package train.model

import train.model.TrainModels.Errors.{TrainAlreadyExists, TrainNotExists}
import train.model.domain.Trains.Train
import train.model.domain.Wagons.Wagon
import train.model.domain.Technology

import scala.util.Either

object TrainModels:

  /** @param description
    *   Error description
    */
  enum Errors(val description: String):
    case TrainAlreadyExists(msg: String)
        extends Errors("Train already exist")
    case TechnologyAlreadyExists(msg: String)
        extends Errors("Technology already exist")
    case TrainNotExists(msg: String)      extends Errors("Train not exist")
    case TechnologyNotExists(msg: String) extends Errors("Technology not exist")
    case Unclassified(msg: String) extends Errors(s"Unclassified error: $msg")

  trait TrainModel:
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
      * @param wagon
      *   wagon type
      * @param wagonCount
      *   wagon amount
      * @return
      *   Returns [[Right]] of Train if train is updated else [[Left]] of *
      *   [[Errors.TrainNotExists]]
      */
    def update(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, Train]

    /** Add new train technology.
      *
      * @param technology
      *   technology to be added
      * @return
      *   [[Right]] of Technology if it is added else [[Left]] of
      *   [[Errors.TrainAlreadyExists]]
      */
    def addTechnology(technology: Technology): Either[Errors, Technology]

    /** Remove technology.
      *
      * @param name
      *   technology name to be removed
      * @return
      *   Returns [[Right]] of List[Technology] if it is removed otherwise
      *   [[Left]] of [[Errors.TrainNotExists]]
      */
    def removeTechnology(name: String): Either[Errors, List[Technology]]

    /** @return
      *   Returns list of trains
      */
    def trains: List[Train]

    def technologies: List[Technology]

  /** Companion object of the trait `TrainModel`.
    *
    * @see
    *   [[TrainModel]] for more detailed behaviour definition.
    */
  object TrainModel:
    private var _trains: List[Train]                   = List.empty
    private var _technologies: Map[String, Technology] = Map.empty

    private def findTrain(name: String): Option[Train] =
      _trains.find(_.name.contentEquals(name))

    def add(train: Train): Either[Errors, Train] =
      findTrain(train.name) match
        case Some(t) =>
          Left[Errors.TrainAlreadyExists, Train](TrainAlreadyExists(t.name))
        case None =>
          // TODO: check that train technology exist
          _trains = train :: _trains
          Right[Errors, Train](train)

    def addTechnology(technology: Technology): Either[Errors, Technology] =
      import Errors.TechnologyAlreadyExists
      _technologies.get(technology.name) match
        case Some(t) =>
          Left[Errors.TechnologyAlreadyExists, Technology](
            TechnologyAlreadyExists(t.name)
          )
        case None =>
          _technologies = _technologies.updated(technology.name, technology)
          Right[Errors, Technology](technology)

    def remove(name: String): Either[Errors, List[Train]] =
      findTrain(name) match
        case Some(_) =>
          _trains = _trains.filterNot(_.name.contentEquals(name))
          Right[Errors, List[Train]](_trains)
        case None =>
          Left[Errors.TrainNotExists, List[Train]](TrainNotExists(name))

    def removeTechnology(name: String): Either[Errors, List[Technology]] =
      import Errors.TechnologyNotExists
      _technologies.get(name) match
        case Some(t) =>
          _technologies = _technologies.removed(name)
          Right[Errors, List[Technology]](technologies)
        case None => Left[Errors, List[Technology]](TechnologyNotExists(name))

    def update(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, Train] =
      findTrain(name) match
        case Some(Train(n, _, _, _)) =>
          val train = Train(
            n,
            technology,
            wagon,
            wagonCount
          )
          remove(n) match
            case Left(e) => Left[Errors.TrainNotExists, Train](
                TrainNotExists(s"$e during update call")
              )
            case Right(_) => add(train)
        case None =>
          Left[Errors.TrainNotExists, Train](Errors.TrainNotExists(name))
        case _ =>
          Left[Errors.Unclassified, Train](
            Errors.Unclassified("train not recognized in match case")
          )

    def trains: List[Train]            = _trains
    def technologies: List[Technology] = _technologies.values.toList
