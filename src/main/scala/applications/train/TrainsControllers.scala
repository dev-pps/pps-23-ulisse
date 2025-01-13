package applications.train

import applications.train.TrainManager.TrainModel
import infrastructures.ui.train.TrainEditor.ViewModel.TrainData

object TrainsControllers:

  enum Errors:
    case SaveEditsError(msg: String)
    case DeleteTrainError(msg: String)

  trait TrainEditorController:
    /** Returns the list of `TrainData`
      * @return
      *   List of [[TrainData]]
      */
    def trainsData: List[TrainData]

    /** @return
      *   List of String containing names of saved train technologies.
      */
    def technologyNames: List[String]

    /** @return
      *   List of String containing names of wagon types.
      */
    def wagonTypeNames: List[String]

    /** Save train edited information. If train does not exist it is saved as
      * new one otherwise its info are updated
      * @param trainData
      *   Train information edits to be saved
      * @return
      *   `Some` type of [[Errors.SaveEditsError]] in case of some errors,
      *   `None` if edits are saved.
      */
    def saveEdits(trainData: TrainData): Option[Errors.SaveEditsError]

    /** Deletes train with a given name if it exists
      * @param trainName
      *   Train name to delete
      * @return
      *   `Some` type of [[Errors.DeleteTrainError]] in case of some deletion
      *   errors.
      */
    def deleteTrain(trainName: String): Option[Errors.DeleteTrainError]

  object TrainEditorController:
    def apply(model: TrainModel): TrainEditorController =
      SimpleTrainController(model)

    private case class SimpleTrainController(model: TrainModel)
        extends TrainEditorController:

      def trainsData: List[TrainData] =
        model.trains.map(t =>
          TrainData(
            name = t.name,
            technologyName = t.techType.name,
            technologyMaxSpeed = t.maxSpeed,
            wagonNameType = t.wagon.use.name,
            wagonCapacity = t.wagon.capacity,
            wagonCount = t.wagonCount
          )
        )

      def technologyNames: List[String] = model.technologies.map(_.name)

      def wagonTypeNames: List[String] = model.wagonTypes.map(_.name)

      def saveEdits(trainData: TrainData): Option[Errors.SaveEditsError] =
        // TODO
        None
        // def validate[T](t: T): Either[Errors, T] =

      def deleteTrain(trainName: String): Option[Errors.DeleteTrainError] =
        if model.trains.map(_.name).contains(trainName) then
          model.remove(trainName) match
            case Left(e)  => Some(Errors.DeleteTrainError(e.description))
            case Right(_) => None
        else
          Some(Errors.DeleteTrainError(msg = "Train not exists"))
