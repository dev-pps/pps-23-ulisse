package ulisse.infrastructures.view.common

import scala.swing.*

object JComponent:

  object JLabel:
    def apply(text: String): JBLabel = JBLabel(Label(text), text)
    case class JBLabel(label: Label, text: String)

  object JTextField:
    def apply(text: String): JBTextField = JBTextField(TextField(text), text)
    case class JBTextField(textField: TextField, text: String)

  object JComoBox:
    def apply[T](items: Seq[T]): JBComoBox[T] = JBComoBox(ComboBox(items), items)
    case class JBComoBox[T](comboBox: ComboBox[T], items: Seq[T])

  object JButton:
    def apply(text: String): JBButton = JBButton(new Button(text), text)
    case class JBButton(button: Button, label: String)

  object JPanel:

    object JFlowPanel:
      def apply(items: Component*): JFlowPanel = JFlowPanel(FlowPanel(items: _*), items: _*)
      case class JFlowPanel(flowPanel: FlowPanel, items: Component*)

    trait JBoxPanel()
    object JBoxPanel:
      def apply(): JBoxPanel = BoxPanelImpl()
      private case class BoxPanelImpl() extends BoxPanel(Orientation.Vertical) with JBoxPanel

    trait JBorderPanel
    object JBorderPanel:
      def apply(): JBorderPanel = BorderPanelImpl()
      private case class BorderPanelImpl() extends BorderPanel with JBorderPanel

    trait JGridPanel
    object JGridPanel:
      def apply(): JGridPanel = GridPanelImpl()
      private case class GridPanelImpl() extends GridPanel(1, 1) with JGridPanel

    trait JGridBagPanel
    object JGridBagPanel:
      def apply(): JGridBagPanel = GridBagPanelImpl()
      private case class GridBagPanelImpl() extends GridBagPanel with JGridBagPanel
