package applicationComponent

import applicationComponent.ApplicationComponents.{Controller, DriverRequirement}

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
//object ApplicationComponents2:
//  // Base traits
//  trait Input
//  trait Output
//  trait Manager
//  trait Controller
//
//  // Basic requirements
//  trait InputPortRequirement[M]:
//    def manager: M
//
//  trait OutputPortRequirement[D]:
//    def driven: D
//
//  trait DriverRequirement[IP]:
//    def inputPort: IP
//
//  trait ManagerRequirement[OP]:
//    def outputPort: OP
//
//  // View base trait
//  trait View[D]:
//    def driver: D
//
//  // Ports collections
//  trait InputPorts[IP <: Input]
//  trait OutputPorts[OP <: Output]
//
//  case class SingleInputPort[IP <: Input](port: IP)             extends InputPorts[IP]
//  case class SingleOutputPort[OP <: Output](port: OP)           extends OutputPorts[OP]
//  case class EmptyInputPorts[IP <: Input]()                     extends InputPorts[IP]
//  case class EmptyOutputPorts[OP <: Output]()                   extends OutputPorts[OP]
//  case class MultipleInputPorts[IP <: Input](ports: List[IP])   extends InputPorts[IP]
//  case class MultipleOutputPorts[OP <: Output](ports: List[OP]) extends OutputPorts[OP]
//
//  // Component traits
//  trait DriverComponent[IP <: Input, V <: View[_]]:
//    def inputPorts: InputPorts[IP]
//    def view: Option[V]
//
//  trait DrivenComponent[OP <: Output]:
//    def outputPorts: OutputPorts[OP]
//
//  trait O1 extends Output
//  trait O2 extends Output
//
//  trait BidirectionalComponent[IP <: Input, OP <: Output, V <: View[_]]
//      extends DriverComponent[IP, V] with O1 with O2
//
//  // Specific implementations
//  case class InputAdapter2(manager: Manager2)                extends Input
//  case class OutputAdapter2(driven: StationEditorController) extends Output
//  case class Manager2(outputPort: OutputAdapter2)            extends Manager
//
//  trait ComponentParams
//  trait ViewComponentParams extends ComponentParams:
//    def name: String
//    def viewBuilder: Option[StationEditorController => StationEditorView]
//
//  case class StationEditorParams(
//      viewBuilder: Option[StationEditorController => StationEditorView] = None
//  ) extends ViewComponentParams
//
//  case class StationEditorView(driver: StationEditorController) extends View[StationEditorController]
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//  case class StationEditorController(
//                                      inputPorts: InputPorts[InputAdapter2],
//                                      outputPorts: OutputPorts[OutputAdapter2],
//                                      params: StationEditorParams
//                                    ) extends Controller, BidirectionalComponent[InputAdapter2, OutputAdapter2, StationEditorView, StationEditorParams]:
//    lazy val view: Option[StationEditorView] = params.viewBuilder.map(_(this))
//
//  // Builder configuration
//  sealed trait ComponentBuilderConfig[C, IP <: Input, M <: Manager, OP <: Output, V <: View[C], P <: ComponentParams]:
//    def build(requirements: InputPortRequirement[M] & ManagerRequirement[OP] & OutputPortRequirement[C]): C
//
//  case class BidirectionalConfig[C, IP <: Input, M <: Manager, OP <: Output, V <: View[C], P <: ViewComponentParams](
//                                      builder: (InputPorts[IP], OutputPorts[OP], P) => C) extends ComponentBuilderConfig[C, IP, M, OP, V, P]:
//    def build(req: InputPortRequirement[M] & ManagerRequirement[OP] & OutputPortRequirement[C]): C =
//      builder(req.inputPorts, req.outputPorts, req.asInstanceOf[ComponentBuilder[C, IP, M, OP, V, P]].params)
//
//  // Component builder
//  case class ComponentBuilder[C, IP <: Input, M <: Manager, OP <: Output, V <: View[C], P <: ComponentParams](
//                                                                                                               componentBuilder: ComponentBuilderConfig[C, IP, M, OP, V, P],
//                                                                                                               inputPortsBuilder: Option[(InputPortRequirement[M]) => InputPorts[IP]],
//                                                                                                               managerBuilder: Option[(ManagerRequirement[OP]) => M],
//                                                                                                               outputPortBuilder: Option[(OutputPortRequirement[C]) => OutputPorts[OP]],
//                                                                                                               params: P
//                                                                                                             ) extends InputPortRequirement[M],
//    ManagerRequirement[OP],
//    OutputPortRequirement[C]:
//
//    lazy val component: C = componentBuilder.build(this)
//    lazy val inputPorts: InputPorts[IP] = inputPortsBuilder.map(_(this)).getOrElse(EmptyInputPorts())
//    lazy val manager: M = managerBuilder.map(_(this)).getOrElse(throw new IllegalStateException("No manager builder"))
//    lazy val outputPort: OP = outputPortBuilder.map(_(this)).getOrElse(throw new IllegalStateException("No output port builder"))
//    lazy val driven: C = component
//
//    def main(args: Array[String]): Unit =
//      // With view
//      val paramsWithView = StationEditorParams(
//        name = "Station Editor",
//        viewBuilder = Some(StationEditorView.apply)
//      )
//
//      val componentWithView = ComponentBuilder(
//        BidirectionalConfig[
//          StationEditorController,
//          InputAdapter2,
//          Manager2,
//          OutputAdapter2,
//          StationEditorView,
//          StationEditorParams
//        ]((inputs, outputs, params) =>
//          StationEditorController(inputs, outputs, params)
//        ),
//        Some(manager => SingleInputPort(InputAdapter2(manager))),
//        Some(outputPort => Manager2(outputPort)),
//        Some(driven => SingleOutputPort(OutputAdapter2(driven))),
//        paramsWithView
//      )
//
//      // Without view
//      val paramsWithoutView = StationEditorParams(
//        name = "Station Editor",
//        viewBuilder = None
//      )
//
//      val componentWithoutView = ComponentBuilder(
//        BidirectionalConfig[
//          StationEditorController,
//          InputAdapter2,
//          Manager2,
//          OutputAdapter2,
//          StationEditorView,
//          StationEditorParams
//        ]((inputs, outputs, params) =>
//          StationEditorController(inputs, outputs, params)
//        ),
//        Some(manager => SingleInputPort(InputAdapter2(manager))),
//        Some(outputPort => Manager2(outputPort)),
//        Some(driven => SingleOutputPort(OutputAdapter2(driven))),
//        paramsWithoutView
//      )
