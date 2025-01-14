package applications.train

import applications.train.TrainManager.{Errors, TrainService}
import entities.train.Technology
import entities.train.Trains.Train
import entities.train.Wagons.{UseType, Wagon}

object TrainPorts:

  /** It represents all possible requests that can be done from external
    * components to application for what concern about `Trains`, trains
    * `Technology` and wagon `UseType`.
    *
    * A default implementation is [[BaseInBoundPort]]
    */
  trait InBound:
    /** Returns the list of `Train`
      * @return
      *   List of `Train`
      */
    def trains: List[Train]

    /** @return
      *   List of saved train `Technology`.
      */
    def technologies: List[Technology]

    /** @return
      *   List of `Wagons.UseType`
      */
    def wagonTypes: List[UseType]

    /** Add/save new train. If train does not exist it is saved as new one
      * otherwise is returned a `Left` of [[Errors]]
      *
      * @param name
      *   Train name
      * @param technologyName
      *   Train technology name
      * @param wagonUseTypeName
      *   Wagon type of use name
      * @param wagonCapacity
      *   Single wagon transport capacity
      * @param wagonCount
      *   Amount of wagons that compose train
      * @return
      *   `Left` type of [[TrainManager.Errors]] in case of some errors,
      *   `Right(List[Train])` if edits are saved.
      */
    def addTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]]

    /** Deletes train with a given name if it exists
      * @param trainName
      *   Train name to delete
      * @return
      *   `Some` type of [[Errors.DeleteTrainError]] in case of some deletion
      *   errors.
      */
    def removeTrain(trainName: String): Either[Errors, List[Train]]

    def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, List[Train]]

  case class BaseInBoundPort(service: TrainService) extends InBound:
    export service.{addTrain => _, *}

    override def addTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]] =
      service.createTrain(
        name,
        technologyName,
        wagonUseTypeName,
        wagonCapacity,
        wagonCount
      )
