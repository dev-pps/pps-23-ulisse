package applicationComponent

import applicationComponent.ApplicationComponents.{Controller, DriverRequirement}
import applicationComponent.ApplicationComponents3.Dependency.Application.userFactory
import applicationComponent.ControllerModule.ControllerImpl

object ApplicationComponents:
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

  trait InputPortRequirement[M]:
    def manager: M

  trait OutputPortRequirement[D]:
    def driven: D

  trait DriverRequirement[IP]:
    def inputPort: IP

  trait ManagerRequirement[OP]:
    def outputPort: OP

  trait Input
  trait Output
  trait Manager
  trait Controller

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
//
object ApplicationComponents2:
  // Base traits
  trait Input
  trait Output
  trait Controller
  trait View:
    def show(): Unit

  // TestImpl
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

object ApplicationComponents3:
  // Base traits
  trait Input
  trait Output
  trait Controller
  trait View:
    def show(): Unit

  // TestImpl
  class ViewImpl(controller: Controller) extends View:
    override def show(): Unit = println("ViewImpl.show")
  class ControllerImpl(using view: View, input: Input) extends Controller with Output
  class InputImpl(using queue: String, output: Output) extends Input

  object Dependency:

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

    @main def mainApp =
      val user = userFactory()

  @main def main3(): Unit =
    given controller: ControllerImpl()
    given InputImpl()
    given String = "queue"
    given view: ViewImpl(controller)

    println(view.show())

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

  @main def main(): Unit =
    view.show(1)
