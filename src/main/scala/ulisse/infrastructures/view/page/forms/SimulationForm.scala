package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.SBorderPanel
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.page.forms.Form.BaseForm

import scala.swing.BorderPanel.Position
import scala.swing.Component

/** Represents the simulation form of the application. */
trait SimulationForm extends Form

/** Companion object of the [[SimulationForm]]. */
object SimulationForm:
  /** Creates a new instance of simulation form. */
  def apply(): SimulationForm = SimulationFormImpl()

  private case class SimulationFormImpl() extends SimulationForm:
    private val mainPanel: SBorderPanel                     = SBorderPanel()
    private val stepSize: ComposedSwing.InfoTextField       = ComposedSwing createInfoTextField "Step"
    private val cyclePerSecond: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Cycle"

    private val playButton  = ExtendedSwing createFormButtonWith ("Play", Styles.formButtonRect)
    private val resetButton = ExtendedSwing createFormButtonWith ("Delete", Styles.formButtonRect)

    private val form = BaseForm("Simulation", stepSize, cyclePerSecond)
    buttonPanel.contents += playButton
    buttonPanel.contents += resetButton

    mainPanel.layout(form.component) = Position.South
    mainPanel.updateGraphics()

    export form.{component => _, _}

    override def component[T >: Component]: T = mainPanel
