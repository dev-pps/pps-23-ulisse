package applications.train

import entities.train.Trains.Train
import entities.train.Technology
import entities.train.Wagons.{UseType, Wagon}
import scala.util.Either

object TrainManager:

  /** @param description
    *   Error description
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
    case Unclassified(msg: String) extends Errors(s"Unclassified error: $msg")

  trait TrainService:
    /** Add train to train collection.
      *
      * @param train
      *   train to be added
      * @return
      *   Returns [[Right]] of `List[Train]` if train is added else [[Left]] of
      *   [[Errors.TrainAlreadyExists]]
      */
    def addTrain(train: Train): Either[Errors, List[Train]]

    /** @param name
      * @param technologyName
      * @param wagonTypeName
      * @param wagonCapacity
      * @param wagonCount
      * @return
      *   Returns [[Right]] of Train if train is added else [[Left]] of
      *   [[Errors.TrainAlreadyExists]]
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
      *   Returns [[Right]] of list of Train if train is removed else [[Left]]
      *   of [[Errors.TrainNotExists]]
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
      *   Returns [[Right]] of Train if train is updated else [[Left]] of *
      *   [[Errors.TrainNotExists]]
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
      *   [[Right]] of Technology if it is added else [[Left]] of
      *   [[Errors.TrainAlreadyExists]]
      */
    def addTechnology(technology: Technology): Either[Errors, List[Technology]]

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

    /** @return
      *   Returns list of train technologies
      */
    def technologies: List[Technology]

    /** @return
      *   Returns List of wagons [[UseType]]
      */
    def wagonTypes: List[UseType]

  /** Companion object of the trait `TrainModel`.
    *
    * @see
    *   [[TrainService]] for more detailed behaviour definition.
    */
  object TrainService:
    def apply(): TrainService = DefaultService()

  private class DefaultService extends TrainService:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _trains: List[Train] = List.empty
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _technologies: Map[String, Technology] = Map.empty

    private def findTrain(name: String): Option[Train] =
      _trains.find(_.name.contentEquals(name))

    private def trainExists(name: String): Either[Errors, List[Train]] =
      if _trains.exists(_.name.contentEquals(name)) then
        Left(Errors.TrainAlreadyExists(name))
      else Right(trains)

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
        _ <- trainExists(name)
        tk <- technologies.find(
          _.name.contentEquals(technologyName)
        ).toRight(Errors.TechnologyNotExists(technologyName))
        w <- wagonTypes.find(_.name.contentEquals(wagonTypeName)).toRight(
          Errors.WagonTypeUnknown(wagonTypeName)
        )
      yield
        _trains =
          Train(name, tk, Wagon(w, wagonCapacity), wagonCount) :: _trains
        _trains

    override def addTechnology(technology: Technology)
        : Either[Errors, List[Technology]] =
      import Errors.TechnologyAlreadyExists
      _technologies.get(technology.name) match
        case Some(t) =>
          Left[Errors.TechnologyAlreadyExists, List[Technology]](
            TechnologyAlreadyExists(t.name)
          )
        case None =>
          _technologies = _technologies.updated(technology.name, technology)
          Right[Errors, List[Technology]](technologies)

    override def removeTrain(name: String): Either[Errors, List[Train]] =
      findTrain(name) match
        case Some(_) =>
          _trains = _trains.filterNot(_.name.contentEquals(name))
          Right[Errors, List[Train]](trains)
        case None =>
          Left[Errors.TrainNotExists, List[Train]](Errors.TrainNotExists(name))

    override def removeTechnology(name: String)
        : Either[Errors, List[Technology]] =
      import Errors.TechnologyNotExists
      _technologies.get(name) match
        case Some(t) =>
          _technologies = _technologies.removed(name)
          Right[Errors, List[Technology]](technologies)
        case None => Left[Errors, List[Technology]](TechnologyNotExists(name))

    override def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, List[Train]] =
//      for
//        train <- findTrain(name)
//        res <- removeTrain(train.name)
//        addRes <- addTrain(Train(
//          name,
//          technology,
//          wagon,
//          wagonCount
//        ))
//      yield addRes
      findTrain(name) match
        case Some(Train(n, _, _, _)) =>
          val train = Train(
            n,
            technology,
            wagon,
            wagonCount
          )
          removeTrain(n) match
            case Left(e) => Left[Errors.TrainNotExists, List[Train]](
                Errors.TrainNotExists(s"$e during update call")
              )
            case Right(_) => addTrain(train)
        case None =>
          Left[Errors.TrainNotExists, List[Train]](Errors.TrainNotExists(name))
        case _ =>
          Left[Errors.Unclassified, List[Train]](
            Errors.Unclassified("train not recognized in match case")
          )

    override def trains: List[Train]            = _trains
    override def technologies: List[Technology] = _technologies.values.toList
    override def wagonTypes: List[UseType]      = UseType.values.toList
