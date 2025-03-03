package ulisse.applications.ports

import ulisse.applications.event.TrainEventQueue
import ulisse.applications.useCases.TrainService
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors.BaseError
import scala.concurrent.Future

object TrainPorts:

  /** It represents all possible requests that can be done from external components to application for what concern
    * about `Trains`, trains `Technology` and wagon `UseType`.
    */
  trait Input:
    /** Returns the list of `Train` */
    def trains: Future[List[Train]]

    /** Returns List of saved train `Technology`. */
    def technologies: Future[List[TrainTechnology]]

    /** Returns List of `Wagons.UseType` */
    def wagonTypes: Future[List[UseType]]

    /** Creates new train given all needed info.
      *
      * Returns updated list of train if train is created successfully otherwise returns a [[BaseError]].
      */
    def createTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

    /** Deletes train given its `name` if it exists.
      *
      * Returns [[Right]] of List[Train] updated if train is deleted otherwise [[Left]] of [[DeleteTrainError]]
      */
    def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]]

    /** Updates train information given its `name`.
      *
      * Returns [[Right]] of List[Train] if train is updated else [[Left]] of
      */
    def updateTrain(name: String)(
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

  object Input:
    def apply(eventQueue: TrainEventQueue): Input = TrainService(eventQueue)
