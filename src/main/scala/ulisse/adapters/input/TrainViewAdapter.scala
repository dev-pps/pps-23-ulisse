package ulisse.adapters.input

import ulisse.applications.ports.TrainPorts
import ulisse.entities.train.Trains.Train
import ulisse.infrastructures.view.train.TrainEditorView
import ulisse.infrastructures.view.train.TrainViewModel.TrainData
import ulisse.utils.Errors.BaseError
import ulisse.infrastructures.view.train.TrainViewModel.{toTechType, toTrainDatas, toWagonNames}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** Adapter of train view.
  *
  * All methods (except `setView`) prepare and send requests to train service port.
  */
trait TrainViewAdapter:
  /** Requests saved trains to service port */
  def requestTrains(): Unit

  /** Requests available technologies */
  def requestTechnologies(): Unit

  /** Requests available wagon types */
  def requestWagonTypes(): Unit

  /** Requests to add new train with the given `trainData` */
  def addTrain(trainData: TrainData): Unit

  /** Requests to delete train with given `name` */
  def deleteTrain(name: String): Unit

  /** Requests to updated train infos. */
  def updateTrain(trainData: TrainData): Unit

  /** Bind view to that adapter. */
  def setView(editorView: TrainEditorView): Unit

object TrainViewAdapter:
  /** Returns [[TrainViewAdapter]] given `trainService` port. */
  def apply(trainService: TrainPorts.Input): TrainViewAdapter =
    BaseAdapter(trainService)

  private final case class BaseAdapter(trainService: TrainPorts.Input)
      extends TrainViewAdapter:
    given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(1)
    )

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var view: Option[TrainEditorView] = None

    override def setView(editorView: TrainEditorView): Unit = view = Some(editorView)

    override def requestTrains(): Unit =
      trainService.trains.handleOnComplete(l =>
        view.onEDT(_.updateTrainList(l.toTrainDatas))
      )

    override def addTrain(trainData: TrainData): Unit =
      trainData.extractThenPerform: (name, tkName, wType, wCap, tLen) =>
        trainService.createTrain(name, tkName, wType, wCap, tLen)

    override def deleteTrain(name: String): Unit =
      trainService.removeTrain(name).handleOnComplete(t => showNewTrainList(t))

    override def updateTrain(trainData: TrainData): Unit =
      trainData.extractThenPerform { (name, tkName, wType, wCap, tLen) =>
        trainService.updateTrain(name)(tkName, wType, wCap, tLen)
      }

    override def requestWagonTypes(): Unit =
      trainService.wagonTypes.handleOnComplete(w => view.onEDT(_.updateWagons(w.toWagonNames)))

    override def requestTechnologies(): Unit =
      trainService.technologies.handleOnComplete(t => view.onEDT(_.updateTechnology(t.toTechType)))

    extension (trainData: TrainData)
      private def extractThenPerform(action: (
          String,
          String,
          String,
          Int,
          Int
      ) => Future[Either[BaseError, List[Train]]]): Unit =
        val data =
          for
            n  <- trainData.name
            tn <- trainData.technologyName
            ts <- trainData.technologyMaxSpeed
            wq <- trainData.wagonCount
            wn <- trainData.wagonNameType
            wc <- trainData.wagonCapacity
          yield (n, tn, wn, wc, wq)
        data match
          case Some(d) => action(d._1, d._2, d._3, d._4, d._5).handleOnComplete(t => showNewTrainList(t))
          case None    => view.onEDT(_.showError("Some field are empty!"))

    extension [T](toComplete: Future[T])
      private def handleOnComplete(onSuccess: T => Unit): Unit =
        toComplete.onComplete {
          case Failure(e) => view.onEDT(_.showError(e.getMessage))
          case Success(r) => onSuccess(r)
        }

    private def showNewTrainList(r: Either[BaseError, List[Train]]): Unit =
      r match
        case Left(err) => view.onEDT(_.showError(s"Error: $err"))
        case Right(t)  => view.onEDT(_.updateTrainList(t.toTrainDatas))

    extension (v: Option[TrainEditorView])
      private def onEDT(f: TrainEditorView => Unit): Unit =
        import scala.swing.Swing
        v.foreach: vw =>
          Swing.onEDT:
            f(vw)
