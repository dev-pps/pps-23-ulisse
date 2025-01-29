package applicationComponent

import applicationComponent.ApplicationComponent4.ControllerModule.ControllerImpl
import applicationComponent.ApplicationComponents3.Application.userFactory

//first experiment trying to implement generic component builder
object ApplicationComponents:

  trait Input
  trait Output
  trait Manager
  trait Controller

  trait InputPortRequirement[M]:
    def manager: M
  trait OutputPortRequirement[D]:
    def driven: D
  trait DriverRequirement[IP]:
    def inputPort: IP
  trait ManagerRequirement[OP]:
    def outputPort: OP
  trait ViewRequirement[C]:
    def controller: C
  trait ControllerRequirement[V]:
    def view: V

  case class Driven1()
  case class OutputAdapter1(driven: Int) extends OutputPortRequirement[Int]
  case class Manager1(outputPort: Int)   extends ManagerRequirement[Int]
  case class InputAdapter1(manager: Int) extends InputPortRequirement[Int]
  case class Driver1(inputPort: Int)     extends DriverRequirement[Int]

  case class InputComponent[Driver, InputPort, Manager](
      driverBuilder: (DriverRequirement[InputPort]) => Driver,
      inputPortBuilder: (InputPortRequirement[Manager]) => InputPort,
      managerBuilder: () => Manager
  ) extends DriverRequirement[InputPort], InputPortRequirement[Manager]:
    val driver: Driver            = driverBuilder(this)
    lazy val inputPort: InputPort = inputPortBuilder(this)
    lazy val manager: Manager     = managerBuilder()

  case class OutputComponent[Manager, OutputPort, Driven](
      managerBuilder: (ManagerRequirement[OutputPort]) => Manager,
      outputPortBuilder: (OutputPortRequirement[Driven]) => OutputPort,
      drivenBuilder: () => Driven
  ) extends ManagerRequirement[OutputPort], OutputPortRequirement[Driven]:
    lazy val manager: Manager       = managerBuilder(this)
    lazy val outputPort: OutputPort = outputPortBuilder(this)
    val driven: Driven              = drivenBuilder()

  case class InputOutputComponent[Driver, InputPort, Manager, OutputPort, Driven](
      driverBuilder: (DriverRequirement[InputPort]) => Driver,
      inputPortBuilder: (InputPortRequirement[Manager]) => InputPort,
      managerBuilder: (ManagerRequirement[OutputPort]) => Manager,
      outputPortBuilder: (OutputPortRequirement[Driven]) => OutputPort,
      drivenBuilder: () => Driven
  ) extends DriverRequirement[InputPort], InputPortRequirement[Manager], ManagerRequirement[OutputPort],
        OutputPortRequirement[Driven]:
    val driver: Driver              = driverBuilder(this)
    lazy val inputPort: InputPort   = inputPortBuilder(this)
    lazy val manager: Manager       = managerBuilder(this)
    lazy val outputPort: OutputPort = outputPortBuilder(this)
    val driven: Driven              = drivenBuilder()

  case class StationEditorController(inputPort: DriverRequirement[InputAdapter2])   extends Controller
  case class InputAdapter2(manager: InputPortRequirement[Manager2])                 extends Input
  case class Manager2(outputPort: ManagerRequirement[OutputAdapter2])               extends Manager
  case class OutputAdapter2(driven: OutputPortRequirement[StationEditorController]) extends Output

  case class BidirectionalComponent[StationEditorController, InputAdapter2, Manager2, OutputAdapter2](
      bidirectionalComponentBuilder: (DriverRequirement[InputAdapter2]) => StationEditorController,
      inputPortBuilder: (InputPortRequirement[Manager2]) => InputAdapter2,
      managerBuilder: (ManagerRequirement[OutputAdapter2]) => Manager2,
      outputPortBuilder: (OutputPortRequirement[StationEditorController]) => OutputAdapter2
  ) extends DriverRequirement[InputAdapter2], InputPortRequirement[Manager2], ManagerRequirement[OutputAdapter2],
        OutputPortRequirement[StationEditorController]:
    val driven: StationEditorController = bidirectionalComponentBuilder(this)
    lazy val inputPort: InputAdapter2   = inputPortBuilder(this)
    lazy val manager: Manager2          = managerBuilder(this)
    lazy val outputPort: OutputAdapter2 = outputPortBuilder(this)

  BidirectionalComponent(StationEditorController.apply, InputAdapter2.apply, Manager2.apply, OutputAdapter2.apply)

//it would have been great if it worked but unfortunately nothing is lazy so it loops
object ApplicationComponents2:

  trait Input
  trait Output
  trait Controller
  trait View:
    def show(): Unit

  class ViewImpl(controller: Controller) extends View:
    override def show(): Unit = println("ViewImpl.show")
  class ControllerImpl(using view: View, input: Input) extends Controller with Output
  class InputImpl(using queue: String, output: Output) extends Input

  @main def main2(): Unit =
    given controller: ControllerImpl()
    given InputImpl()
    given String = "queue"
    given view: ViewImpl(controller)
    println(view.show()) // broken

//first example of cake pattern and self types
object ApplicationComponents3:
  trait Logger:
    def log(s: String): Unit

  class LoggerImpl extends Logger:
    def log(s: String): Unit = println(s)

  trait LoggerDependency:
    val logger: Logger

  trait User:
    def loggableOperation: Unit

  trait UserComponent:
    loggerDependency: LoggerDependency =>
    class UserImpl extends User:
      def loggableOperation: Unit = loggerDependency.logger.log(" hello !")

  object Application extends LoggerDependency with UserComponent:
    override val logger: Logger = new LoggerImpl
    def userFactory(): User     = new UserImpl

  @main def main3 =
    val user = userFactory()

//complete example of cake pattern and self types
object ApplicationComponent4:
  object ModelModule:
    trait Model:
      def m(): Int
    trait Provider:
      val model: Model
    case class ModelImpl() extends Model:
      def m() = 1
    trait Component:
      val model: Model = ModelImpl()
    trait Interface extends Provider with Component

  object ViewModule:
    trait View:
      def show(i: Int): Unit
    trait Provider:
      val view: View
    type Requirements = ControllerModule.Provider

    trait Component:
      context: Requirements =>
      case class ViewImpl() extends View:
        def show(i: Int): Unit = println(i)
        def update(): Unit     = context.controller.notifyChange(" changhed ")

    trait Interface extends Provider with Component:
      self: Requirements =>

  object ControllerModule:
    type Requirements = ViewModule.Provider with ModelModule.Provider
    trait Controller:
      val context: Requirements
      def notifyChange(s: String): Unit
    trait Provider:
      val controller: Controller
    case class ControllerImpl(context: Requirements) extends Controller:
      def notifyChange(s: String): Unit =
        context.view.show(context.model.m())

  object MVC extends ModelModule.Interface
      with ViewModule.Interface
      with ControllerModule.Provider:
    override val controller: ControllerImpl = ControllerImpl(this)
    override val view: ViewImpl             = ViewImpl()

    @main def main4(): Unit =
      view.show(1)

//complete example of cake pattern with constructor DI
object ApplicationComponents5:
  trait Model[S <: Model.State]:
    def state: S
    def update(f: S => S): Unit

  object Model:
    trait State
    def apply[S <: State](initialState: S): Model[S] =
      new Model[S]:
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        private var _state                   = initialState
        override def state: S                = _state
        override def update(f: S => S): Unit = _state = f(_state)

    trait Requirements[S <: State]

    trait Provider[S <: State]:
      def model: Model[S]

  trait Controller[State <: Model.State]:
    def state: State

  object Controller:
    type Factory[V <: View[?], C <: Controller[?], S <: Model.State] = Requirements[V, S] => C
    trait Requirements[V <: View[?], S <: Model.State] extends Model.Provider[S] with View.Provider[V]

    trait Dependencies[V <: View[S], S <: Model.State](requirements: Requirements[V, S]) extends Controller[S]:
      protected def view: V         = requirements.view
      protected def model: Model[S] = requirements.model

    trait Provider[C <: Controller[?]]:
      def controller: C

  abstract class BaseController[V <: View[S], S <: Model.State](requirements: Controller.Requirements[V, S])
      extends Controller[S]
      with Controller.Dependencies(requirements):

    override def state: S = model.state

  class EmptyController[State <: Model.State, V <: View[State]](requirements: Controller.Requirements[V, State])
      extends BaseController(requirements)

  trait View[State <: Model.State]:
    def state: State
    def show(): Unit
    def hide(): Unit
    def displayMessage(message: String): Unit
    def updateState(state: State): Unit = ()

  object View:
    type Factory[C <: Controller[?], V <: View[?]] = Requirements[C] => V
    trait Requirements[C <: Controller[?]] extends Controller.Provider[C]
    trait Dependencies[C <: Controller[?]](requirements: Requirements[C]) extends View[?]:
      protected def controller: C = requirements.controller
    trait Provider[V <: View[?]]:
      def view: V

  abstract class BaseView[State <: Model.State, C <: Controller[State]](requirements: View.Requirements[C])
      extends View[State]
      with View.Dependencies(requirements):

    override def state: State = _state
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _state: State = controller.state
    override def updateState(state: State): Unit =
      _state = state

  final case class ApplicationState(game: Option[String]) extends Model.State

  object ApplicationState:
    def apply(): ApplicationState = ApplicationState(Option.empty)

  trait HomeController extends Controller[ApplicationState]

  object HomeController:
    def apply(requirements: Controller.Requirements[HomeView, ApplicationState]): HomeController =
      new EmptyController(requirements) with HomeController

  trait HomeView extends View[ApplicationState]
  case class HomeViewImpl(requirements: View.Requirements[HomeController])
      extends BaseView[ApplicationState, HomeController](requirements) with HomeView:
    override def show(): Unit                          = println("HomeViewImpl.show")
    override def hide(): Unit                          = println("HomeViewImpl.hide")
    override def displayMessage(message: String): Unit = println("HomeViewImpl.displayMessage")

  case class ApplicationPage[S <: Model.State, C <: Controller[?], V <: View[?]](
      override val model: Model[S],
      viewFactory: View.Factory[C, V],
      controllerFactory: Controller.Factory[V, C, S]
  ) extends Model.Requirements[S]
      with View.Requirements[C]
      with Controller.Requirements[V, S]:
    override lazy val view: V       = viewFactory(this)
    override lazy val controller: C = controllerFactory(this)

  val page = ApplicationPage(Model(ApplicationState()), HomeViewImpl.apply, HomeController.apply)

//CONCLUSION: maybe we can group together the components that will satisfy
//some common View.Provider but since our components are in general very
//different with each other in term of single component requirements
//is not possible to make a simple component wrapper like ApplicationPage
//later if some common pattern is discover there are rooms to rethink a possible factorization
//e.g. mixing using params for independent components like queue
//(so linear dependencies can be satisfied directly)
//and use bidirectional builder for circular dependencies
